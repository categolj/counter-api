use actix_web::{web, App, HttpServer, HttpResponse, Responder, middleware::Logger};
use diesel::prelude::*;
use diesel::r2d2::{self, ConnectionManager};
use serde::{Deserialize, Serialize};
use std::env;
use env_logger;
use dotenv::dotenv;

mod schema;

use schema::counters;

#[derive(Deserialize)]
struct CounterRequest {
    #[serde(rename = "entryId")]
    entry_id: i64,
}

#[derive(Serialize)]
struct CounterResponse {
    counter: i64,
}

#[derive(Queryable, Insertable, AsChangeset)]
#[diesel(table_name = counters)]
struct Counter {
    entry_id: i64,
    counter: i64,
}

type DbPool = r2d2::Pool<ConnectionManager<PgConnection>>;

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    dotenv().ok();
    env_logger::init();

    let database_url = env::var("DATABASE_URL").expect("DATABASE_URL must be set");
    let manager = ConnectionManager::<PgConnection>::new(database_url);
    let pool = r2d2::Pool::builder()
        .build(manager)
        .expect("Failed to create pool.");

    let port = env::var("PORT").unwrap_or_else(|_| "7777".to_string());
    let port: u16 = port.parse().expect("PORT must be a number");

    HttpServer::new(move || {
        App::new()
            .app_data(web::Data::new(pool.clone()))
            .wrap(Logger::default())
            .route("/counter", web::post().to(increment_counter))
    })
    .bind(("127.0.0.1", port))?
    .run()
    .await
}

async fn increment_counter(pool: web::Data<DbPool>, req: web::Json<CounterRequest>) -> impl Responder {
    let conn = pool.get().expect("Couldn't get db connection from pool");
    let mut conn = conn;  // Mutable reference to the connection

    let entry_id = req.entry_id;
    let result = web::block(move || increment_counter_in_db(&mut conn, entry_id)).await;

    match result {
        Ok(counter) => HttpResponse::Ok().json(CounterResponse { counter: counter.expect("Failed to increment counter") }),
        Err(_) => HttpResponse::InternalServerError().finish(),
    }
}

fn increment_counter_in_db(conn: &mut PgConnection, entry_id: i64) -> Result<i64, diesel::result::Error> {
    use self::counters::dsl::{counters, entry_id as column_entry_id};

    conn.transaction(|conn| {
        let current_counter = counters
            .filter(column_entry_id.eq(entry_id))
            .first::<Counter>(conn)
            .optional()?;

        match current_counter {
            Some(mut counter_record) => {
                counter_record.counter += 1;
                diesel::update(counters.find(entry_id))
                    .set(&counter_record)
                    .execute(conn)?;
                Ok(counter_record.counter)
            },
            None => {
                let new_counter = Counter { entry_id, counter: 1 };
                diesel::insert_into(counters)
                    .values(&new_counter)
                    .execute(conn)?;
                Ok(new_counter.counter)
            }
        }
    })
}

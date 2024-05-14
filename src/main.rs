use actix_web::{web, App, HttpServer, HttpResponse, Responder, middleware::Logger};
use serde::{Deserialize, Serialize};
use std::sync::Mutex;
use std::collections::HashMap;
use std::env;
use env_logger::Env;

#[derive(Deserialize)]
struct CounterRequest {
    #[serde(rename = "entryId")]
    entry_id: u64,
}

#[derive(Serialize)]
struct CounterResponse {
    counter: u64,
}

struct AppState {
    counters: Mutex<HashMap<u64, u64>>,
}

#[actix_web::main]
async fn main() -> std::io::Result<()> {
    // Initialize the logger with a default level of info
    env_logger::Builder::from_env(Env::default().default_filter_or("info")).init();

    let port = env::var("PORT").unwrap_or_else(|_| "7777".to_string());
    let port: u16 = port.parse().expect("PORT must be a number");

    let app_state = web::Data::new(AppState {
        counters: Mutex::new(HashMap::new()),
    });

    HttpServer::new(move || {
        App::new()
            .app_data(app_state.clone())
            // Enable logger middleware
            .wrap(Logger::default())
            .route("/counter", web::post().to(increment_counter))
    })
    .bind(("127.0.0.1", port))?
    .run()
    .await
}

async fn increment_counter(state: web::Data<AppState>, req: web::Json<CounterRequest>) -> impl Responder {
    let mut counters = state.counters.lock().unwrap();
    let counter = counters.entry(req.entry_id).or_insert(0);
    *counter += 1;
    HttpResponse::Ok().json(CounterResponse { counter: *counter })
}


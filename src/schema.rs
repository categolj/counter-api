// @generated automatically by Diesel CLI.

diesel::table! {
    counters (entry_id) {
        entry_id -> Int8,
        counter -> Int8,
    }
}

package db.migration.mysql

class V1_1__Migrate_To_Database extends db.migration.common.V1_1__Migrate_To_Database {
  override val insertStatement: String = s"INSERT INTO ${tablePrefix}Key_Value_Store_Value (id, key_text, data) VALUES (?, ?, ?)"
}

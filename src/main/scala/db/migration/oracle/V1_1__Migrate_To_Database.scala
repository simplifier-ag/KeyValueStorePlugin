package db.migration.oracle

class V1_1__Migrate_To_Database extends  db.migration.common.V1_1__Migrate_To_Database {
  override val insertStatement: String = s"INSERT INTO ${tablePrefix}KEY_VALUE_STORE_VALUE (ID, KEY_TEXT, DATA) VALUES (?, ?, ?)"
}

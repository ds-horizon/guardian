package com.dreamsportslabs.guardian.dao.query;

public class ContactBlockSql {

  public static final String UPSERT_CONTACT_BLOCK =
      "INSERT INTO contact_api_blocks (tenant_id, contact, api_path, reason, operator, unblocked_at, is_active) "
          + "VALUES (?, ?, ?, ?, ?, ?, ?) "
          + "ON DUPLICATE KEY UPDATE "
          + "reason = VALUES(reason), "
          + "operator = VALUES(operator), "
          + "unblocked_at = VALUES(unblocked_at), "
          + "updated_at = CURRENT_TIMESTAMP";

  public static final String UNBLOCK_CONTACT_API =
      "UPDATE contact_api_blocks SET is_active = 0 WHERE tenant_id = ? AND contact = ? AND api_path = ?";

  public static final String GET_ACTIVE_BLOCKS_BY_CONTACT =
      "SELECT contact, api_path, unblocked_at FROM contact_api_blocks WHERE tenant_id = ? AND contact = ? AND is_active = 1";

  public static final String CHECK_API_BLOCKED =
      "SELECT COUNT(*) as count FROM contact_api_blocks WHERE tenant_id = ? AND contact = ? AND api_path = ? AND is_active = 1 AND (unblocked_at > UNIX_TIMESTAMP())";
}

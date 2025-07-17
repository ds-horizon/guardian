package com.dreamsportslabs.guardian.dao.query;

public class UserFlowBlockSql {

  public static final String UPSERT_USER_FLOW_BLOCK =
      "INSERT INTO user_flow_block_config (tenant_id, user_identifier, flow_name, reason, unblocked_at, is_active) "
          + "VALUES (?, ?, ?, ?, ?, ?) "
          + "ON DUPLICATE KEY UPDATE "
          + "reason = VALUES(reason), "
          + "unblocked_at = VALUES(unblocked_at), "
          + "is_active = VALUES(is_active), "
          + "updated_at = CURRENT_TIMESTAMP";

  public static final String UNBLOCK_USER_FLOW =
      "UPDATE user_flow_block_config SET is_active = 0 WHERE tenant_id = ? AND user_identifier = ? AND flow_name = ?";

  // TODO: We can also add reason for blocked flows in the response to the tenant
  public static final String GET_ACTIVE_FLOW_BLOCKS_BY_USER_IDENTIFIER =
      "SELECT flow_name AS flowName FROM user_flow_block_config WHERE tenant_id = ? AND user_identifier = ? AND is_active = 1 AND (unblocked_at > UNIX_TIMESTAMP())";

  public static final String GET_FLOW_BLOCK_REASON_BATCH =
      "SELECT reason FROM user_flow_block_config WHERE tenant_id = ? AND flow_name = ? AND user_identifier IN (%s) AND is_active = 1 AND (unblocked_at > UNIX_TIMESTAMP())";
}

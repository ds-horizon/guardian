package com.dreamsportslabs.guardian.dao.query;

public class ContactFlowBlockSql {

    public static final String UPSERT_CONTACT_FLOW_BLOCK =
            "INSERT INTO contact_flow_blocks (tenant_id, contact, flow_name, reason, operator, unblocked_at, is_active) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE "
                    + "reason = VALUES(reason), "
                    + "operator = VALUES(operator), "
                    + "unblocked_at = VALUES(unblocked_at), "
                    + "is_active = VALUES(is_active), "
                    + "updated_at = CURRENT_TIMESTAMP";

    public static final String UNBLOCK_CONTACT_FLOW =
            "UPDATE contact_flow_blocks SET is_active = 0 WHERE tenant_id = ? AND contact = ? AND flow_name = ?";

    public static final String GET_ACTIVE_FLOW_BLOCKS_BY_CONTACT =
            "SELECT contact, flow_name, unblocked_at FROM contact_flow_blocks WHERE tenant_id = ? AND contact = ? AND is_active = 1";

    public static final String CHECK_FLOW_BLOCKED =
            "SELECT COUNT(*) as count FROM contact_flow_blocks WHERE tenant_id = ? AND contact = ? AND flow_name = ? AND is_active = 1 AND (unblocked_at > UNIX_TIMESTAMP())";
} 
package com.zhanghui.config.typehandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.*;
import java.util.Objects;

/**
 * @author: ZhangHui
 * @date: 2020/10/27 22:38
 * @version：1.0
 */
public class DatetimeTypeHandler extends BaseTypeHandler<Long> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        if (Objects.equals(parameter, 0L)) {
            ps.setTimestamp(i, null);
        } else {
            ps.setTimestamp(i, new Timestamp(parameter));
        }
    }

    @Override
    public Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (rs.getObject(columnName) instanceof Timestamp) {
            return rs.getTimestamp(columnName) == null ? 0L : rs.getTimestamp(columnName).getTime();
        }
        return rs.getLong(columnName);
    }

    @Override
    public Long getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (rs.getObject(columnIndex) instanceof Timestamp) {
            return rs.getTimestamp(columnIndex) == null ? 0L : rs.getTimestamp(columnIndex).getTime();
        }
        return rs.getLong(columnIndex);
    }

    @Override
    public Long getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (cs.getObject(columnIndex) instanceof Timestamp) {
            return cs.getTimestamp(columnIndex) == null ? 0L : cs.getTimestamp(columnIndex).getTime();
        }
        return cs.getLong(columnIndex);
    }
}

package io.spring.infrastructure.mybatis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import org.apache.ibatis.type.JdbcType;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DateTimeHandlerTest {

  private DateTimeHandler handler;

  @Mock private PreparedStatement preparedStatement;
  @Mock private ResultSet resultSet;
  @Mock private CallableStatement callableStatement;

  @BeforeEach
  void setUp() {
    handler = new DateTimeHandler();
  }

  @Test
  void should_set_parameter_with_non_null_value() throws Exception {
    DateTime dateTime = new DateTime(2023, 1, 15, 10, 30, 0, DateTimeZone.UTC);

    handler.setParameter(preparedStatement, 1, dateTime, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), any(Timestamp.class), any());
  }

  @Test
  void should_set_parameter_with_null_value() throws Exception {
    handler.setParameter(preparedStatement, 1, null, JdbcType.TIMESTAMP);

    verify(preparedStatement).setTimestamp(eq(1), eq(null), any());
  }

  @Test
  void should_get_result_by_column_name_non_null() throws Exception {
    Timestamp ts = new Timestamp(1673782200000L);
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(ts);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_name_null() throws Exception {
    when(resultSet.getTimestamp(eq("created_at"), any())).thenReturn(null);

    DateTime result = handler.getResult(resultSet, "created_at");

    assertNull(result);
  }

  @Test
  void should_get_result_by_column_index_non_null() throws Exception {
    Timestamp ts = new Timestamp(1673782200000L);
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(resultSet, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_by_column_index_null() throws Exception {
    when(resultSet.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(resultSet, 1);

    assertNull(result);
  }

  @Test
  void should_get_result_from_callable_statement_non_null() throws Exception {
    Timestamp ts = new Timestamp(1673782200000L);
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(ts);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNotNull(result);
    assertEquals(ts.getTime(), result.getMillis());
  }

  @Test
  void should_get_result_from_callable_statement_null() throws Exception {
    when(callableStatement.getTimestamp(eq(1), any())).thenReturn(null);

    DateTime result = handler.getResult(callableStatement, 1);

    assertNull(result);
  }
}

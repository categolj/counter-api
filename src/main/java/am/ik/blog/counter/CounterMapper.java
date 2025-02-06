package am.ik.blog.counter;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CounterMapper {

	private final JdbcClient jdbcClient;

	public CounterMapper(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	@Transactional
	public Counter increment(int entryId) {
		return this.jdbcClient.sql("""
				INSERT INTO counters (entry_id, counter)
				VALUES (?, 1)
				ON CONFLICT (entry_id)
				DO UPDATE
				  SET counter = counters.counter + 1
				RETURNING entry_id, counter
				""").param(entryId).query(Counter.class).single();
	}

	public List<Counter> getAll() {
		return this.jdbcClient.sql("""
				SELECT entry_id, counter FROM counters ORDER BY counter DESC
				""").query(Counter.class).list();
	}

}

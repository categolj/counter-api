package am.ik.blog.counter.web;

import am.ik.blog.counter.Counter;
import am.ik.blog.counter.CounterMapper;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CounterController {

	private final CounterMapper counterMapper;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CounterController(CounterMapper counterMapper) {
		this.counterMapper = counterMapper;
	}

	@PostMapping(path = "/counter")
	public CounterResponse increment(@RequestBody CounterRequest request) {
		Counter counter = this.counterMapper.increment(request.entryId());
		logger.atInfo()
			.addKeyValue("entryId", request.entryId())
			.addKeyValue("counter", counter.counter())
			.log("Counter incremented");
		return new CounterResponse(counter.counter());
	}

	@GetMapping(path = "/counter")
	public List<Counter> getAll() {
		return this.counterMapper.getAll();
	}

	public record CounterRequest(int entryId) {
	}

	public record CounterResponse(long counter) {
	}

}

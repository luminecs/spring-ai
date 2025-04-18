package org.springframework.ai.model.function;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MethodInvokingFunctionCallbackTests {

	private static final Map<String, Object> arguments = new ConcurrentHashMap<>();

	String value = """
			{
			  "unit": "CELSIUS",
			  "city": "Barcelona",
			  "intNumber": 123,
			  "record": {
				"foo": "foo",
				"bar": "bar"
			  },
			  "intList": [1, 2, 3]
			}
			""";

	@BeforeEach
	public void beforeEach() {
		arguments.clear();
	}

	@Test
	public void staticMethod() throws NoSuchMethodException, SecurityException {

		var functionCallback = FunctionCallback.builder()
			.method("myStaticMethod", String.class, Unit.class, int.class, MyRecord.class, List.class)
			.description("weather at location")
			.objectMapper(new ObjectMapper())
			.targetClass(TestClassWithFunctionMethods.class)
			.build();

		String response = functionCallback.call(this.value);

		assertThat(response).isEqualTo("23");

		assertThat(arguments).hasSize(5);
		assertThat(arguments.get("city")).isEqualTo("Barcelona");
		assertThat(arguments.get("unit")).isEqualTo(Unit.CELSIUS);
		assertThat(arguments.get("intNumber")).isEqualTo(123);
		assertThat(arguments.get("record")).isEqualTo(new MyRecord("foo", "bar"));
		assertThat(arguments.get("intList")).isEqualTo(List.of(1, 2, 3));
	}

	@Test
	public void nonStaticMethod() throws NoSuchMethodException, SecurityException {

		var object = new TestClassWithFunctionMethods();

		var functionCallback = FunctionCallback.builder()
			.method("myNonStaticMethod", String.class, Unit.class, int.class, MyRecord.class, List.class)
			.description("weather at location")
			.targetObject(object)
			.build();

		String response = functionCallback.call(this.value);

		assertThat(response).isEqualTo("23");

		assertThat(arguments).hasSize(5);
		assertThat(arguments.get("city")).isEqualTo("Barcelona");
		assertThat(arguments.get("unit")).isEqualTo(Unit.CELSIUS);
		assertThat(arguments.get("intNumber")).isEqualTo(123);
		assertThat(arguments.get("record")).isEqualTo(new MyRecord("foo", "bar"));
		assertThat(arguments.get("intList")).isEqualTo(List.of(1, 2, 3));
	}

	@Test
	public void noArgsNoReturnMethod() throws NoSuchMethodException, SecurityException {

		var functionCallback = FunctionCallback.builder()
			.method("argumentLessReturnVoid")
			.description("weather at location")
			.objectMapper(new ObjectMapper())
			.targetClass(TestClassWithFunctionMethods.class)
			.build();

		String response = functionCallback.call(this.value);

		assertThat(response).isEqualTo("Done");

		assertThat(arguments.get("method called")).isEqualTo("argumentLessReturnVoid");
	}

	record MyRecord(String foo, String bar) {
	}

	public enum Unit {

		CELSIUS, FAHRENHEIT

	}

	public static class TestClassWithFunctionMethods {

		public static void argumentLessReturnVoid() {
			arguments.put("method called", "argumentLessReturnVoid");
		}

		public static String myStaticMethod(String city, Unit unit, int intNumber, MyRecord record,
				List<Integer> intList) {
			System.out.println("City: " + city + " Unit: " + unit + " intNumber: " + intNumber + " Record: " + record
					+ " List: " + intList);

			arguments.put("city", city);
			arguments.put("unit", unit);
			arguments.put("intNumber", intNumber);
			arguments.put("record", record);
			arguments.put("intList", intList);

			return "23";
		}

		public String myNonStaticMethod(String city, Unit unit, int intNumber, MyRecord record, List<Integer> intList) {
			System.out.println("City: " + city + " Unit: " + unit + " intNumber: " + intNumber + " Record: " + record
					+ " List: " + intList);

			arguments.put("city", city);
			arguments.put("unit", unit);
			arguments.put("intNumber", intNumber);
			arguments.put("record", record);
			arguments.put("intList", intList);

			return "23";
		}

	}

}

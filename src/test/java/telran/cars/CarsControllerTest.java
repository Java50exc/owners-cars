package telran.cars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import telran.cars.dto.*;
import static telran.cars.api.ValidationConstants.*;
import telran.cars.exceptions.NotFoundException;
import telran.cars.service.CarsService;

@WebMvcTest // inserting into Application Context Mock WEB server instead of real WebServer
class CarsControllerTest {
	private static final String[] BODY_METHODS = new String[] { "POST", "PUT" };

	private static final String PERSON_PATH = "http://localhost:8080/cars/person";
	private static final String CAR_PATH = "http://localhost:8080/cars";
	private static final String PURCHASE_PATH = "http://localhost:8080/cars/trade";

	private static final long PERSON_ID = 123000l;
	private static final String PERSON_NAME = "Somename";
	private static final String PERSON_BIRTH_DATE = "2000-10-10";
	private static final String PERSON_NOT_FOUND_MESSAGE = "person not found";
	private static final String PERSON_ALREADY_EXISTS_MESSAGE = "person already exists";
	private static final String CAR_ALREADY_EXISTS_MESSAGE = "car already exists";
	private static final String CAR_NOT_FOUND_MESSAGE = "car not found";
	private static final String WRONG_EMAIL_ADDRESS = "kuku";
	private static final String EMAIL_ADDRESS = "vasya@gmail.com";
	private static final Long WRONG_PERSON_ID_MIN = 123l;
	private static final Long WRONG_PERSON_ID_MAX = 9999999l;
	private static final String EMPTY_STRING = "";
	private static final String WRONG_BIRTH_DATE = "10-10-2000";
	

	private static final String CAR_MODEL = "Somemodel";
	private static final String CAR_NUMBER = "123-01-002";
	private static final String WRONG_CAR_NUMBER = "123-010-002";

	@MockBean // inserting into Application Context Mock instead of real Service
				// implementation
	CarsService carsService;
	@Autowired // for injection of MockMvc from Application Context
	MockMvc mockMvc;
	CarDto carDto = new CarDto(CAR_NUMBER, "model");
	CarDto carDto1 = new CarDto("car123", "mode123");

	@Autowired // for injection of ObjectMapper from Application context
	ObjectMapper mapper; // object for getting JSON from object and object from JSON
	private PersonDto personDto = new PersonDto(PERSON_ID, "Vasya", "2000-10-10", EMAIL_ADDRESS);
	PersonDto personDtoUpdated = new PersonDto(PERSON_ID, "Vasya", "2000-10-10", "vasya@tel-ran.com");
	TradeDealDto tradeDeal = new TradeDealDto(CAR_NUMBER, PERSON_ID);

	PersonDto personNoId = new PersonDto(null, PERSON_NAME, PERSON_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personWrongIdMin = new PersonDto(WRONG_PERSON_ID_MIN, PERSON_NAME, PERSON_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personWrongIdMax = new PersonDto(WRONG_PERSON_ID_MAX, PERSON_NAME, PERSON_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personEmptyName = new PersonDto(PERSON_ID, EMPTY_STRING, PERSON_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personNullName = new PersonDto(PERSON_ID, null, PERSON_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personEmptyBirthDate = new PersonDto(PERSON_ID, PERSON_NAME, EMPTY_STRING, EMAIL_ADDRESS);
	PersonDto personNullBirthDate = new PersonDto(PERSON_ID, PERSON_NAME, null, EMAIL_ADDRESS);
	PersonDto personWrongBirthDate = new PersonDto(PERSON_ID, PERSON_NAME, WRONG_BIRTH_DATE, EMAIL_ADDRESS);
	PersonDto personEmptyEmail = new PersonDto(PERSON_ID, PERSON_NAME, PERSON_BIRTH_DATE, EMPTY_STRING);
	PersonDto personNullEmail = new PersonDto(PERSON_ID, PERSON_NAME, PERSON_BIRTH_DATE, null);
	PersonDto personWrongEmail = new PersonDto(PERSON_ID, PERSON_NAME, PERSON_BIRTH_DATE, WRONG_EMAIL_ADDRESS);
	PersonDto personMissingAllFields = new PersonDto(null, null, null, null);
	PersonDto personWrongAllFields = new PersonDto(WRONG_PERSON_ID_MIN, EMPTY_STRING, WRONG_BIRTH_DATE,
			WRONG_EMAIL_ADDRESS);

	CarDto carEmptyNumber = new CarDto(EMPTY_STRING, CAR_MODEL);
	CarDto carNullNumber = new CarDto(null, CAR_MODEL);
	CarDto carWrongNumber = new CarDto(WRONG_CAR_NUMBER, CAR_MODEL);
	CarDto carEmptyModel = new CarDto(CAR_NUMBER, EMPTY_STRING);
	CarDto carNullModel = new CarDto(CAR_NUMBER, null);
	CarDto carMissingAllFields = new CarDto(null, null);
	CarDto carWrongAllFields = new CarDto(WRONG_CAR_NUMBER, EMPTY_STRING);

	TradeDealDto tradeDealEmptyNumber = new TradeDealDto(EMPTY_STRING, PERSON_ID);
	TradeDealDto tradeDealNullNumber = new TradeDealDto(null, PERSON_ID);
	TradeDealDto tradeDealWrongNumber = new TradeDealDto(WRONG_CAR_NUMBER, PERSON_ID);
	TradeDealDto tradeDealWrongNumberMin = new TradeDealDto(CAR_NUMBER, WRONG_PERSON_ID_MIN);
	TradeDealDto tradeDealWrongNumberMax = new TradeDealDto(CAR_NUMBER, WRONG_PERSON_ID_MAX);
	TradeDealDto tradeDealMissingAllFields = new TradeDealDto(null, null);
	TradeDealDto tradeDealWrongAllFields = new TradeDealDto(WRONG_CAR_NUMBER, WRONG_PERSON_ID_MAX);

	@BeforeAll
	static void init() {
		Arrays.sort(BODY_METHODS);
	}

	@Test
	void testAddCar() throws Exception {
		when(carsService.addCar(carDto)).thenReturn(carDto);
		String jsonCarDto = mapper.writeValueAsString(carDto); // conversion from carDto object to string JSON
		String actualJSON = mockMvc
				.perform(post(CAR_PATH).contentType(MediaType.APPLICATION_JSON).content(jsonCarDto))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonCarDto, actualJSON);

	}

	@Test
	void testAddPerson() throws Exception {
		when(carsService.addPerson(personDto)).thenReturn(personDto);
		String jsonPersonDto = mapper.writeValueAsString(personDto); // conversion from carDto object to string JSON
		String actualJSON = mockMvc
				.perform(post(PERSON_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonPersonDto))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonPersonDto, actualJSON);
	}

	@Test
	void testUpdatePerson() throws Exception {
		when(carsService.updatePerson(personDtoUpdated)).thenReturn(personDtoUpdated);
		String jsonPersonDtoUpdated = mapper.writeValueAsString(personDtoUpdated); // conversion from carDto object to
																					// string JSON
		String actualJSON = mockMvc
				.perform(put(PERSON_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonPersonDtoUpdated))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonPersonDtoUpdated, actualJSON);
	}

	@Test
	void testPurchase() throws Exception {
		when(carsService.purchase(tradeDeal)).thenReturn(tradeDeal);
		String jsonTradeDeal = mapper.writeValueAsString(tradeDeal);
		String actualJSON = mockMvc
				.perform(put(PURCHASE_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonTradeDeal))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonTradeDeal, actualJSON);
	}

	@Test
	void testDeletePerson() throws Exception {
		when(carsService.deletePerson(PERSON_ID)).thenReturn(personDto);
		String jsonExpected = mapper.writeValueAsString(personDto);
		String actualJSON = mockMvc.perform(delete(PERSON_PATH + "/" + PERSON_ID))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonExpected, actualJSON);
	}

	@Test
	void testDeleteCar() throws Exception {
		when(carsService.deleteCar(CAR_NUMBER)).thenReturn(carDto);
		String jsonExpected = mapper.writeValueAsString(carDto);
		String actualJSON = mockMvc.perform(delete(CAR_PATH + "/" + CAR_NUMBER))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonExpected, actualJSON);
	}

	@Test
	void testGetOwnerCars() throws Exception {
		CarDto[] expectedArray = { carDto, carDto1 };
		String jsonExpected = mapper.writeValueAsString(expectedArray);
		when(carsService.getOwnerCars(PERSON_ID)).thenReturn(Arrays.asList(expectedArray));
		String actualJSON = mockMvc.perform(get(PERSON_PATH + "/" + PERSON_ID))
				.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		assertEquals(jsonExpected, actualJSON);
	}

	@Test
	void testGetCarOwner() throws Exception {
		when(carsService.getCarOwner(CAR_NUMBER)).thenReturn(personDto);
		String jsonExpected = mapper.writeValueAsString(personDto);
		String actualJSON = mockMvc.perform(get(CAR_PATH + "/" + CAR_NUMBER)).andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		assertEquals(jsonExpected, actualJSON);
	}

	/******************************************************************/
	/*********** ALternative flows - Service Exceptions Handling *************/
	@Test
	void testDeletePersonNotFound() throws Exception {
		when(carsService.deletePerson(PERSON_ID)).thenThrow(new NotFoundException(PERSON_NOT_FOUND_MESSAGE));

		String actualJSON = mockMvc.perform(delete(PERSON_PATH + "/" + PERSON_ID))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(PERSON_NOT_FOUND_MESSAGE, actualJSON);

	}

	@Test
	void testAddPersonAlreadyExists() throws Exception {
		when(carsService.addPerson(personDto)).thenThrow(new IllegalStateException(PERSON_ALREADY_EXISTS_MESSAGE));
		String jsonPersonDto = mapper.writeValueAsString(personDto); // conversion from carDto object to string JSON
		String response = mockMvc
				.perform(post(PERSON_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonPersonDto))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertEquals(PERSON_ALREADY_EXISTS_MESSAGE, response);
	}

	@Test
	void testAddCarAlreadyExists() throws Exception {
		when(carsService.addCar(carDto)).thenThrow(new IllegalStateException(CAR_ALREADY_EXISTS_MESSAGE));
		String jsonCarDto = mapper.writeValueAsString(carDto); // conversion from carDto object to string JSON
		String response = mockMvc
				.perform(post(CAR_PATH).contentType(MediaType.APPLICATION_JSON).content(jsonCarDto))
				.andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();
		assertEquals(CAR_ALREADY_EXISTS_MESSAGE, response);

	}

	@Test
	void testUpdatePersonNotFound() throws Exception {
		when(carsService.updatePerson(personDtoUpdated)).thenThrow(new NotFoundException(PERSON_NOT_FOUND_MESSAGE));
		String jsonPersonDtoUpdated = mapper.writeValueAsString(personDtoUpdated); // conversion from carDto object to
																					// string JSON
		String response = mockMvc
				.perform(put(PERSON_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonPersonDtoUpdated))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(PERSON_NOT_FOUND_MESSAGE, response);
	}

	@Test
	void testPurchaseCarNotFound() throws Exception {
		testPurchaseNotFound(CAR_NOT_FOUND_MESSAGE);
	}

	@Test
	void testPurchasePersonNotFound() throws Exception {
		testPurchaseNotFound(PERSON_NOT_FOUND_MESSAGE);
	}

	private void testPurchaseNotFound(String message)
			throws JsonProcessingException, UnsupportedEncodingException, Exception {
		when(carsService.purchase(tradeDeal)).thenThrow(new NotFoundException(message));
		String jsonTradeDeal = mapper.writeValueAsString(tradeDeal);
		String response = mockMvc
				.perform(put(PURCHASE_PATH).contentType(MediaType.APPLICATION_JSON)
						.content(jsonTradeDeal))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(message, response);
	}

	@Test
	void testDeleteCarNotFound() throws Exception {
		when(carsService.deleteCar(CAR_NUMBER)).thenThrow(new NotFoundException(CAR_NOT_FOUND_MESSAGE));
		String response = mockMvc.perform(delete(CAR_PATH + "/" + CAR_NUMBER))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(CAR_NOT_FOUND_MESSAGE, response);
	}

	@Test
	void testGetOwnerCarsPersonNotFound() throws Exception {

		when(carsService.getOwnerCars(PERSON_ID)).thenThrow(new NotFoundException(PERSON_NOT_FOUND_MESSAGE));
		String response = mockMvc.perform(get(PERSON_PATH + "/" + PERSON_ID))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(PERSON_NOT_FOUND_MESSAGE, response);
	}

	@Test
	void testGetCarOwnerCarNotFound() throws Exception {
		when(carsService.getCarOwner(CAR_NUMBER)).thenThrow(new NotFoundException(CAR_NOT_FOUND_MESSAGE));
		String response = mockMvc.perform(get(CAR_PATH + "/" + CAR_NUMBER))
				.andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();
		assertEquals(CAR_NOT_FOUND_MESSAGE, response);
	}

	/*****************************************************************************/
	/* Alternative flows - Validation exceptions handling ***********************/
	@Test
	void addPerson_noId_getMissingMsg() {
		validationTestHandler(personNoId, post(PERSON_PATH), MISSING_PERSON_ID_MESSAGE);
	}

	@Test
	void addPerson_idLessMin_getWrongMsg() {
		validationTestHandler(personWrongIdMin, post(PERSON_PATH), WRONG_MIN_PERSON_ID_VALUE_MESSAGE);
	}

	@Test
	void addPerson_idGreaterMax_getWrongMsg() {
		validationTestHandler(personWrongIdMax, post(PERSON_PATH), WRONG_MAX_PERSON_ID_VALUE_MESSAGE);
	}

	@Test
	void addPerson_emptyName_getMissingMsg() {
		validationTestHandler(personEmptyName, post(PERSON_PATH), MISSING_PERSON_NAME_MESSAGE);
	}

	@Test
	void addPerson_nullName_getMissingMsg() {
		validationTestHandler(personNullName, post(PERSON_PATH), MISSING_PERSON_NAME_MESSAGE);
	}

	@Test
	void addPerson_emptyBirthDate_getMissingMsg() {
		validationTestHandler(personEmptyBirthDate, post(PERSON_PATH),
				getSortedMessages(MISSING_BIRTH_DATE_MESSAGE, WRONG_DATE_FORMAT_MESSAGE));
	}

	@Test
	void addPerson_nullBirthDate_getMissingMsg() {
		validationTestHandler(personNullBirthDate, post(PERSON_PATH), MISSING_BIRTH_DATE_MESSAGE);
	}

	@Test
	void addPerson_wrongBirthDate_getWrongMsg() {
		validationTestHandler(personWrongBirthDate, post(PERSON_PATH), WRONG_DATE_FORMAT_MESSAGE);
	}

	@Test
	void addPerson_emptyEmail_getMissingMsg() {
		validationTestHandler(personEmptyEmail, post(PERSON_PATH), MISSING_PERSON_EMAIL_MESSAGE);
	}

	@Test
	void addPerson_nullEmail_getMissingMsg() {
		validationTestHandler(personNullEmail, post(PERSON_PATH), MISSING_PERSON_EMAIL_MESSAGE);
	}

	@Test
	void addPerson_wrongEmail_getWrongMsg() {
		validationTestHandler(personWrongEmail, post(PERSON_PATH), WRONG_EMAIL_FORMAT_MESSAGE);
	}

	@Test
	void addPerson_missingAllFields_getMissingMsgs() {
		validationTestHandler(personMissingAllFields, post(PERSON_PATH), getSortedMessages(MISSING_PERSON_ID_MESSAGE,
				MISSING_PERSON_NAME_MESSAGE, MISSING_BIRTH_DATE_MESSAGE, MISSING_PERSON_EMAIL_MESSAGE));
	}

	@Test
	void addPerson_wrongAllFields_getWrongMsgs() {
		validationTestHandler(personWrongAllFields, post(PERSON_PATH),
				getSortedMessages(WRONG_MIN_PERSON_ID_VALUE_MESSAGE, MISSING_PERSON_NAME_MESSAGE,
						WRONG_DATE_FORMAT_MESSAGE, WRONG_EMAIL_FORMAT_MESSAGE));
	}

	@Test
	void addCar_emptyNumber_getMissingMsgs() {
		validationTestHandler(carEmptyNumber, post(CAR_PATH),
				getSortedMessages(MISSING_CAR_NUMBER_MESSAGE, WRONG_CAR_NUMBER_MESSAGE));
	}

	@Test
	void addCar_nullNumber_getMissingMsg() {
		validationTestHandler(carNullNumber, post(CAR_PATH), MISSING_CAR_NUMBER_MESSAGE);
	}

	@Test
	void addCar_wrongNumber_getWrongMsg() {
		validationTestHandler(carWrongNumber, post(CAR_PATH), WRONG_CAR_NUMBER_MESSAGE);
	}

	@Test
	void addCar_emptyModel_getMissingMsg() {
		validationTestHandler(carEmptyModel, post(CAR_PATH), MISSING_CAR_MODEL_MESSAGE);
	}

	@Test
	void addCar_nullModel_getMissingMsg() {
		validationTestHandler(carNullModel, post(CAR_PATH), MISSING_CAR_MODEL_MESSAGE);
	}

	@Test
	void addCar_missingAllFields_getMissingMsgs() {
		validationTestHandler(carMissingAllFields, post(CAR_PATH),
				getSortedMessages(MISSING_CAR_NUMBER_MESSAGE, MISSING_CAR_MODEL_MESSAGE));
	}

	@Test
	void addCar_wrongAllFields_getWrongMsgs() {
		validationTestHandler(carWrongAllFields, post(CAR_PATH),
				getSortedMessages(WRONG_CAR_NUMBER_MESSAGE, MISSING_CAR_MODEL_MESSAGE));
	}

	@Test
	void purchase_emptyNumber_getMissingMsgs() {
		validationTestHandler(tradeDealEmptyNumber, put(PURCHASE_PATH),
				getSortedMessages(MISSING_CAR_NUMBER_MESSAGE, WRONG_CAR_NUMBER_MESSAGE));
	}

	@Test
	void purchase_nullNumber_getMissingMsg() {
		validationTestHandler(tradeDealNullNumber, put(PURCHASE_PATH), MISSING_CAR_NUMBER_MESSAGE);
	}

	@Test
	void purchase_wrongNumber_getWrongMsg() {
		validationTestHandler(tradeDealWrongNumber, put(PURCHASE_PATH), WRONG_CAR_NUMBER_MESSAGE);
	}

	@Test
	void purchase_idLessMin_getWrongMsg() {
		validationTestHandler(tradeDealWrongNumberMin, put(PURCHASE_PATH), WRONG_MIN_PERSON_ID_VALUE_MESSAGE);
	}

	@Test
	void purchase_idGreaterMax_getWrongMsg() {
		validationTestHandler(tradeDealWrongNumberMax, put(PURCHASE_PATH), WRONG_MAX_PERSON_ID_VALUE_MESSAGE);
	}

	@Test
	void purchase_missingAllFields_getMissingMsg() {
		validationTestHandler(tradeDealMissingAllFields, put(PURCHASE_PATH), MISSING_CAR_NUMBER_MESSAGE);
	}

	@Test
	void purchase_wrongAllFields_getWrongMsgs() {
		validationTestHandler(tradeDealWrongAllFields, put(PURCHASE_PATH),
				getSortedMessages(WRONG_CAR_NUMBER_MESSAGE, WRONG_MAX_PERSON_ID_VALUE_MESSAGE));
	}

	@Test
	void updatePerson_wrongAllFields_getWrongMsgs() {
		validationTestHandler(personWrongAllFields, put(PERSON_PATH),
				getSortedMessages(WRONG_MIN_PERSON_ID_VALUE_MESSAGE, MISSING_PERSON_NAME_MESSAGE,
						WRONG_DATE_FORMAT_MESSAGE, WRONG_EMAIL_FORMAT_MESSAGE));
	}
	
	@Test
	void deletePerson_idLessMin_getWrongMsg() {
		validationTestHandler(null, delete(PERSON_PATH + "/" + WRONG_PERSON_ID_MIN), WRONG_MIN_PERSON_ID_VALUE_MESSAGE);
	}
	
	@Test
	void deletePerson_idGreaterMax_getWrongMsg() {
		validationTestHandler(null, delete(PERSON_PATH + "/" + WRONG_PERSON_ID_MAX), WRONG_MAX_PERSON_ID_VALUE_MESSAGE);
	}
	
	@Test
	void deletePerson_idNull_getMissingMsg() {
		validationTestHandler(null, delete(PERSON_PATH + "/"+ null), MISSING_PERSON_ID_MESSAGE);
	}
	
//	@Test
//	void deletePerson_idEmpty_getMissingMsg() {
//		validationTestHandler(null, delete(PERSON_PATH + ""), MISSING_PERSON_ID_MESSAGE);
//	}
	
//	@Test
//	void deleteCar_numberEmpty_getMissingMsg() {
//		validationTestHandler(null, delete(CAR_PATH + ""), getSortedMessages(MISSING_CAR_NUMBER_MESSAGE, WRONG_CAR_NUMBER_MESSAGE));
//	}
	
	@Test
	void deleteCar_numberNull_getMissingMsg() {
		validationTestHandler(null, delete(CAR_PATH + "/" + null), WRONG_CAR_NUMBER_MESSAGE);
	}
	
	@Test
	void deleteCar_numberWrong_getWrongMsg() {
		validationTestHandler(null, delete(CAR_PATH + "/" + WRONG_CAR_NUMBER), WRONG_CAR_NUMBER_MESSAGE);
	}
	
//	@Test
//	void getOwnerCars_idEmpty_getMissingMsg() {
//		validationTestHandler(null, get(PERSON_PATH + "/" + EMPTY_STRING), MISSING_PERSON_ID_MESSAGE);
//	}
	
//	@Test
//	void getOwnerCars_idNull_getMissingMsg() {
//		validationTestHandler(null, get(PERSON_PATH + null), MISSING_PERSON_ID_MESSAGE);
//	}
	
	@Test
	void getOwnerCars_idLessMin_getWrongMsg() {
		validationTestHandler(null, get(PERSON_PATH + "/" + WRONG_PERSON_ID_MIN), WRONG_MIN_PERSON_ID_VALUE_MESSAGE);
	}
	
	@Test
	void getOwnerCars_idGreaterMax_getWrongMsg() {
		validationTestHandler(null, get(PERSON_PATH + "/" + WRONG_PERSON_ID_MAX), WRONG_MAX_PERSON_ID_VALUE_MESSAGE);
	}
	
//	@Test
//	void getCarOwner_numberEmpty_getMissingMsg() {
//		validationTestHandler(null, get(CAR_PATH + EMPTY_STRING), getSortedMessages(MISSING_CAR_NUMBER_MESSAGE, WRONG_CAR_NUMBER_MESSAGE));
//	}
	
	@Test
	void getCarOwner_numberNull_getMissingMsg() {
		validationTestHandler(null, get(CAR_PATH + "/" + null), WRONG_CAR_NUMBER_MESSAGE);
	}
	
	@Test
	void getCarOwner_numberWrong_getWrongMsg() {
		validationTestHandler(null, get(CAR_PATH + "/" + WRONG_CAR_NUMBER), WRONG_CAR_NUMBER_MESSAGE);
	}
	

	private String getSortedMessages(String... messages) {
		Arrays.sort(messages);
		return String.join(";", messages);

	}


	private void validationTestHandler(Object dto, MockHttpServletRequestBuilder request, String expectedMsg) {
		String response = null;
		try {
			if (Arrays.binarySearch(BODY_METHODS, request.buildRequest(null).getMethod()) >= 0) {
				String json = mapper.writeValueAsString(dto);
				request = request.contentType(MediaType.APPLICATION_JSON).content(json);
			}
			response = mockMvc.perform(request).andExpect(status().isBadRequest()).andReturn().getResponse()
					.getContentAsString();
		} catch (Exception e) {
		}
		assertEquals(expectedMsg, response);
	}



}

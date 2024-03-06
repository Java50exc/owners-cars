package telran.cars.api;

import java.util.Arrays;

import telran.cars.dto.CarState;

public interface ValidationConstants {
	String MISSING_CAR_NUMBER_MESSAGE = "Missing car number";
	String CAR_NUMBER_REGEXP = "(\\d{3}-\\d{2}-\\d{3})|(\\d{2}-\\d{3}-\\d{2})";
//	String CAR_STATE_REGEXP = String.join("|", Arrays.stream(CarState.values()).map(CarState::toString).toArray(String[]::new));
	String CAR_STATE_REGEXP = "OLD|NEW|GOOD|MIDDLE|BAD";
	String WRONG_CAR_NUMBER_MESSAGE = "Incorrect Car Number";
	String MISSING_CAR_MODEL_MESSAGE = "Missing car model";
	String MISSING_CAR_YEAR_MESSAGE = "Missing car year";
	String MISSING_CAR_COMPANY_MESSAGE = "Missing car company";
	String MISSING_CAR_ENGINE_POWER_MESSAGE = "Missing car engine power";
	String MISSING_CAR_ENGINE_CAPACITY_MESSAGE = "Missing car engine capacity";
	String NEGATIVE_CAR_KILOMETERS_MESSAGE = "Car kilometers must be greater or equal 0";
	String NEGATIVE_CAR_ENGINE_POWER_MESSAGE = "Car engine power must be greater or equal 0";
	String NEGATIVE_CAR_ENGINE_CAPACITY_MESSAGE = "Car engine capacity must be greater or equal 0";
	String WRONG_CAR_STATE_MESSAGE = "Car state must be: " + CAR_STATE_REGEXP;
	String MISSING_PERSON_ID_MESSAGE = "Missing person ID";
	long MIN_PERSON_ID_VALUE = 100000l;
	long MAX_PERSON_ID_VALUE = 999999l;
	long MIN_MODEL_YEAR_VALUE = 1900l;
	String WRONG_MIN_PERSON_ID_VALUE = "Person ID must be greater or equal " + MIN_PERSON_ID_VALUE;
	String WRONG_MAX_PERSON_ID_VALUE = "Person ID must be less or equal " + MAX_PERSON_ID_VALUE;
	String WRONG_MIN_MODEL_YEAR_VALUE_MESSAGE = "Model year must be greater or equal" + MIN_MODEL_YEAR_VALUE;
	String MISSING_PERSON_NAME_MESSAGE = "Missing person name";
	String MISSING_BIRTH_DATE_MESSAGE = "Missing person's birth date";
	String BIRTH_DATE_REGEXP = "\\d{4}-\\d{2}-\\d{2}";
	String WRONG_DATE_FORMAT = "Wrong date format, must be YYYY-MM-dd";
	String MISSING_PERSON_EMAIL = "Missing email address";
	String WRONG_EMAIL_FORMAT = "Wrong email format";
	String WRONG_TRADE_DEAL_DATE = "Trade deal date must be in past or present";
}

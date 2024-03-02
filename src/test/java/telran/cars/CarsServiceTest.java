package telran.cars;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import telran.cars.dto.*;
import telran.cars.exceptions.NotFoundException;
import telran.cars.service.CarsService;
import telran.cars.service.model.Car;
import telran.cars.service.model.CarOwner;

@SpringBootTest
class CarsServiceTest {
	private static final String MODEL = "model";
	private static final String CAR_NUMBER_1 = "111-11-111";
	private static final String CAR_NUMBER_2 = "222-22-222";
	private static final String CAR_NUMBER_NOT_EXISTS = "333-22-222";
	private static final Long PERSON_ID_1 = 123l;
	private static final String NAME1 = "name1";
	private static final String BIRTH_DATE_1 = "2000-10-10";
	private static final String EMAIL1 = "name1@gmail.com";
	private static final Long PERSON_ID_2 = 124l;
	private static final String NAME2 = "name2";
	private static final String BIRTH_DATE_2 = "2000-10-10";
	private static final String EMAIL2 = "name2@gmail.com";
	private static final String EMAIL3 = "name3@gmail.com";
	private static final Long PERSON_ID_NOT_EXISTS = 1111111111L;
	CarDto car1 = new CarDto(CAR_NUMBER_1, MODEL);
	CarDto car2 = new CarDto(CAR_NUMBER_2, MODEL);
	CarDto carNotExists = new CarDto(CAR_NUMBER_NOT_EXISTS, MODEL);
	PersonDto personDtoNotExists = new PersonDto(PERSON_ID_NOT_EXISTS, NAME1, BIRTH_DATE_1, EMAIL1);
	PersonDto personDtoNotExistsUpd = new PersonDto(PERSON_ID_NOT_EXISTS, NAME1, BIRTH_DATE_1, EMAIL1);
	PersonDto personDto1 = new PersonDto(PERSON_ID_1, NAME1, BIRTH_DATE_1, EMAIL1);
	PersonDto personDto1Upd = new PersonDto(PERSON_ID_1, NAME1, BIRTH_DATE_1, EMAIL3);
	PersonDto personDto2 = new PersonDto(PERSON_ID_2, NAME2, BIRTH_DATE_2, EMAIL2);
	TradeDealDto tradeDealDto1 = new TradeDealDto(CAR_NUMBER_1, PERSON_ID_1);
	TradeDealDto tradeDealDto2 = new TradeDealDto(CAR_NUMBER_2, PERSON_ID_2);
	TradeDealDto tradeDealDtoWithoutPerson = new TradeDealDto(CAR_NUMBER_1, null);
	TradeDealDto tradeDealDto3 = new TradeDealDto(CAR_NUMBER_1, PERSON_ID_2);
	TradeDealDto tradeDealPersonNotExists = new TradeDealDto(CAR_NUMBER_1, PERSON_ID_NOT_EXISTS);
	TradeDealDto tradeDealCarNotExists = new TradeDealDto(CAR_NUMBER_NOT_EXISTS, PERSON_ID_1);

	@Autowired
	CarsService carsService;

	@BeforeEach
	void setUp() throws Exception {
		var owners = carsService.getClass().getDeclaredField("owners");
		var cars = carsService.getClass().getDeclaredField("cars");
		owners.setAccessible(true);
		cars.setAccessible(true);
		owners.set(carsService, new HashMap<Long, CarOwner>());
		cars.set(carsService, new HashMap<Long, Car>());

		carsService.addCar(car1);
		carsService.addCar(car2);
		carsService.addPerson(personDto1);
		carsService.addPerson(personDto2);
		carsService.purchase(tradeDealDto1);
		carsService.purchase(tradeDealDto2);
	}

	@Test
	void addPerson_correctFlow_success() {
		assertEquals(personDtoNotExists, carsService.addPerson(personDtoNotExists));
		assertThrowsExactly(IllegalStateException.class, () -> carsService.addPerson(personDtoNotExists));
	}
	
	@Test
	void addCar_correctFlow_success() {
		assertEquals(carNotExists, carsService.addCar(carNotExists));
		assertThrowsExactly(IllegalStateException.class, () -> carsService.addCar(carNotExists));
	}
	
	@Test
	void updatePerson_correctFlow_success() {
		assertEquals(personDto1Upd, carsService.updatePerson(personDto1Upd));
	}
	
	@Test
	void updatePerson_personNotFound_throwsException() {
		assertThrowsExactly(NotFoundException.class, () -> carsService.updatePerson(personDtoNotExistsUpd));
	}
	
	@Test
	void deletePerson_correctFlow_success() {
		assertEquals(personDto1, carsService.deletePerson(PERSON_ID_1));
		assertThrowsExactly(NotFoundException.class, () -> carsService.deletePerson(PERSON_ID_1));
	}
	
	@Test
	void deleteCar_correctFlow_success() {
		assertEquals(car1, carsService.deleteCar(CAR_NUMBER_1));
		assertThrowsExactly(NotFoundException.class, () -> carsService.deleteCar(CAR_NUMBER_1));
	}
	
	@Test
	void purchase_emptyPerson_success() {
		assertEquals(tradeDealDtoWithoutPerson, carsService.purchase(tradeDealDtoWithoutPerson));
		assertThrowsExactly(IllegalStateException.class, () -> carsService.addCar(car1));
		assertThrowsExactly(IllegalStateException.class, () -> carsService.addPerson(personDto1));
		assertNull(carsService.getCarOwner(CAR_NUMBER_1));
		assertFalse(carsService.getOwnerCars(PERSON_ID_1).contains(car1));
	}
	
	@Test
	void purchase_correctFlow_success() {
		assertEquals(tradeDealDto3, carsService.purchase(tradeDealDto3));
		assertEquals(personDto2, carsService.getCarOwner(CAR_NUMBER_1));
		assertFalse(carsService.getOwnerCars(PERSON_ID_1).contains(car1));

	}
	
	@Test
	void purchase_personNotFound_throwsException() {
		assertThrowsExactly(NotFoundException.class, () -> carsService.purchase(tradeDealPersonNotExists));
		assertEquals(personDto1, carsService.getCarOwner(CAR_NUMBER_1));
		assertTrue(carsService.getOwnerCars(PERSON_ID_1).contains(car1));
	}
	
	@Test
	void purchase_carNotFound_throwsException() {
		List<CarDto> cars = carsService.getOwnerCars(PERSON_ID_1);
		assertThrowsExactly(NotFoundException.class, () -> carsService.purchase(tradeDealCarNotExists));
		assertEquals(cars, carsService.getOwnerCars(PERSON_ID_1));
	}


	@Test
	void getOwnerCars_correctFlow_success() {
		assertEquals(List.of(car1), carsService.getOwnerCars(PERSON_ID_1));
	}
	
	@Test
	void getOwnerCars_noCars_success() {
		carsService.purchase(tradeDealDtoWithoutPerson);
		assertEquals(0, carsService.getOwnerCars(PERSON_ID_1).size());
	}
	
	@Test
	void getOwnerCars_ownerNotFound_throwsException() {
		assertThrowsExactly(NotFoundException.class, () -> carsService.getOwnerCars(PERSON_ID_NOT_EXISTS));
	}

	@Test
	void getCarOwner_correctFlow_success() {
		assertEquals(personDto1, carsService.getCarOwner(CAR_NUMBER_1));
	}
	
	@Test
	void getCarOwner_carNotFound_throwsException() {
		assertThrowsExactly(NotFoundException.class, () -> carsService.getCarOwner(CAR_NUMBER_NOT_EXISTS));
	}
	
	@Test
	void getCarOwner_noOwner_success() {
		carsService.purchase(tradeDealDtoWithoutPerson);
		assertNull(carsService.getCarOwner(CAR_NUMBER_1));
	}

}

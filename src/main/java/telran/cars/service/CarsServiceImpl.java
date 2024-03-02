package telran.cars.service;

import java.util.*;
import static telran.cars.api.ServiceConstants.*;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import telran.cars.dto.*;
import telran.cars.exceptions.NotFoundException;
import telran.cars.service.model.*;

@Service
@Slf4j
public class CarsServiceImpl implements CarsService {
	HashMap<Long, CarOwner> owners = new HashMap<>();
	HashMap<String, Car> cars = new HashMap<>();

	@Override
	public PersonDto addPerson(PersonDto personDto) {
		log.debug("addPerson: received data: {}", personDto);
		CarOwner owner = new CarOwner(personDto);
		if (owners.putIfAbsent(owner.getId(), owner) != null) {
			log.error("{} already exists", personDto);
			throw new IllegalStateException(PERSON_ALREADY_EXISTS_MESSAGE);
		}
		log.debug("addPerson: {} added", personDto);
		return personDto;
	}

	@Override
	public CarDto addCar(CarDto carDto) {
		log.debug("addCar: received data: {}", carDto);
		Car car = new Car(carDto);
		if (cars.putIfAbsent(car.getNumber(), car) != null) {
			log.error("{} already exists", carDto);
			throw new IllegalStateException(CAR_ALREADY_EXISTS_MESSAGE);
		}
		log.debug("addCar: {} added", carDto);
		return carDto;
	}

	@Override
	public PersonDto updatePerson(PersonDto personDto) {
		log.debug("updatePerson: received data: {}", personDto);
		CarOwner owner = new CarOwner(personDto);
		CarOwner co = owners.computeIfPresent(owner.getId(), (k, v) -> {
			if (!owner.getName().equals(v.getName())) {
				log.error("updatePerson: name modification");
				throw new IllegalStateException(PERSON_NAME_MODIFICATION_MESSAGE);
			}
			if (!owner.getBirthDate().equals(v.getBirthDate())) {
				log.error("updatePerson: birth date modification");
				throw new IllegalStateException(PERSON_BIRTH_DATE_MODIFICATION_MESSAGE);
			}
			v.setEmail(owner.getEmail());
			return v;
		});

		if (co == null) {
			log.error("updatePerson {} not found", personDto);
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		log.debug("updatePerson: updated {}", personDto);
		return personDto;
	}

	@Override
	public PersonDto deletePerson(long id) {
		log.debug("deletePerson: received data: {}", id);
		CarOwner owner = owners.remove(id);

		if (owner == null) {
			log.error("deletePerson {} not found", id);
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		owner.getCars().stream().forEach(e -> e.setOwner(null));
		log.debug("deletePerson: deleted {}", id);
		return owner.build();
	}

	@Override
	public CarDto deleteCar(String carNumber) {
		log.debug("deleteCar: received data: {}", carNumber);
		Car car = cars.remove(carNumber);

		if (car == null) {
			log.error("deleteCar: {} not found", carNumber);
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}

		if (car.getOwner() != null) {
			car.getOwner().getCars().remove(car);
		}
		log.debug("deleteCar: deleted {}", carNumber);
		return car.build();
	}

	@Override
	public TradeDealDto purchase(TradeDealDto tradeDeal) {
		log.debug("purchase: received data: {}", tradeDeal);
		Car car = cars.get(tradeDeal.carNumber());
		CarOwner newOwner = null;

		if (car == null) {
			log.error("purchase: {} not found", tradeDeal.carNumber());
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}

		if (tradeDeal.personId() != null) {
			newOwner = owners.get(tradeDeal.personId());

			if (newOwner == null) {
				log.error("purchase: {} not found", tradeDeal.personId());
				throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
			}
			newOwner.getCars().add(car);
		}

		if (car.getOwner() != null) {
			car.getOwner().getCars().remove(car);
		}
		car.setOwner(newOwner);
		log.debug("purchase: purchaised {}", tradeDeal);
		return tradeDeal;
	}

	@Override
	public List<CarDto> getOwnerCars(long id) {
		log.debug("getOwnerCars: received data: {}", id);
		CarOwner owner = owners.get(id);

		if (owner == null) {
			log.error("getOwnerCars: {} not found", id);
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		log.debug("getOwnerCars: returned data: {}", id);
		return owner.getCars().stream().map(c -> c.build()).toList();
	}

	@Override
	public PersonDto getCarOwner(String carNumber) {
		log.debug("getCarOwner: received data: {}", carNumber);
		Car car = cars.get(carNumber);
		
		if (car == null) {
			log.error("getCarOwner: {} not found", carNumber);
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}
		if (car.getOwner() != null) {
			log.debug("getCarOwner: returned data: {}", car.getOwner().build());
			return car.getOwner().build();
		}
		log.warn("getCarOwner: returned data: owner not found");
		return null;
	}

}

package telran.cars.service;

import java.util.*;
import static telran.cars.api.ServiceConstants.*;

import org.springframework.stereotype.Service;

import telran.cars.dto.*;
import telran.cars.exceptions.NotFoundException;
import telran.cars.service.model.*;

@Service
public class CarsServiceImpl implements CarsService {
	HashMap<Long, CarOwner> owners = new HashMap<>();
	HashMap<String, Car> cars = new HashMap<>();

	@Override
	public PersonDto addPerson(PersonDto personDto) {
		CarOwner owner = new CarOwner(personDto);
		if (owners.putIfAbsent(owner.getId(), owner) != null) {
			throw new IllegalStateException(PERSON_ALREADY_EXISTS_MESSAGE);
		}
		return personDto;
	}

	@Override
	public CarDto addCar(CarDto carDto) {
		Car car = new Car(carDto);

		if (cars.putIfAbsent(car.getNumber(), car) != null) {
			throw new IllegalStateException(CAR_ALREADY_EXISTS_MESSAGE);
		}
		return carDto;
	}

	@Override
	public PersonDto updatePerson(PersonDto personDto) {
		CarOwner owner = new CarOwner(personDto);

		CarOwner co = owners.computeIfPresent(owner.getId(), (k, v) -> {
			if (!owner.getName().equals(v.getName())) {
				throw new IllegalStateException(PERSON_NAME_MODIFICATION_MESSAGE);
			}
			if (!owner.getBirthDate().equals(v.getBirthDate())) {
				throw new IllegalStateException(PERSON_BIRTH_DATE_MODIFICATION_MESSAGE);
			}
			v.setEmail(owner.getEmail());
			return v;
		});

		if (co == null) {
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		return personDto;
	}

	@Override
	public PersonDto deletePerson(long id) {
		CarOwner owner = owners.remove(id);

		if (owner == null) {
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		owner.getCars().stream().forEach(e -> e.setOwner(null));
		return owner.build();
	}

	@Override
	public CarDto deleteCar(String carNumber) {
		Car car = cars.remove(carNumber);

		if (car == null) {
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}

		if (car.getOwner() != null) {
			car.getOwner().getCars().remove(car);
		}
		return car.build();
	}

	@Override
	public TradeDealDto purchase(TradeDealDto tradeDeal) {
		Car car = cars.get(tradeDeal.carNumber());
		CarOwner newOwner = null;

		if (car == null) {
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}

		if (tradeDeal.personId() != null) {
			newOwner = owners.get(tradeDeal.personId());

			if (newOwner == null) {
				throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
			}
			newOwner.getCars().add(car);
		}

		if (car.getOwner() != null) {
			car.getOwner().getCars().remove(car);
		}
		car.setOwner(newOwner);
		return tradeDeal;
	}

	@Override
	public List<CarDto> getOwnerCars(long id) {
		CarOwner owner = owners.get(id);

		if (owner == null) {
			throw new NotFoundException(PERSON_NOT_FOUND_MESSAGE);
		}
		return owner.getCars().stream().map(c -> c.build()).toList();
	}

	@Override
	public PersonDto getCarOwner(String carNumber) {
		Car car = cars.get(carNumber);
		
		if (car == null) {
			throw new NotFoundException(CAR_NOT_FOUND_MESSAGE);
		}
		if (car.getOwner() != null) {
			return car.getOwner().build();
		}
		return null;
	}

}

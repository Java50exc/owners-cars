package telran.cars.service;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import telran.cars.dto.*;
import telran.cars.exceptions.NotFoundException;
import telran.cars.service.model.*;

@Slf4j
@Service("carsService")
@Scope("prototype")
public class CarsServiceImpl implements CarsService {
	HashMap<Long, CarOwner> owners = new HashMap<>();
	HashMap<String, Car> cars = new HashMap<>();
	HashMap<Car, List<TradeDeal>> trades = new HashMap<>();

	ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	Lock rLock = lock.readLock();
	Lock wLock = lock.writeLock();

	@Override
	public PersonDto addPerson(PersonDto personDto) {
		long id = personDto.id();
		try {
			wLock.lock();

			if (owners.containsKey(id)) {
				throw new IllegalStateException(String.format("person  %d already exists", id));
			}
			owners.put(id, new CarOwner(personDto));
		} finally {
			wLock.unlock();
		}
		return personDto;
	}

	@Override
	public CarDto addCar(CarDto carDto) {
		String carNumber = carDto.number();
		try {
			wLock.lock();
			
			if (cars.containsKey(carNumber)) {
				throw new IllegalStateException(String.format("car %s already exists", carNumber));
			}
			cars.put(carNumber, new Car(carDto));
			
		} finally {
			wLock.unlock();
		}
		return carDto;
	}

	@Override
	public PersonDto updatePerson(PersonDto personDto) {
		long id = personDto.id();
		try {
			wLock.lock();
			hasCarOwner(id);
			CarOwner carOwner = owners.get(id);
			carOwner.setEmail(personDto.email());
		} finally {
			wLock.unlock();
		}
		return personDto;
	}

	@Override
	public PersonDto deletePerson(long id) {
		hasCarOwner(id);
		
		try {
			wLock.lock();
			CarOwner carOwner = owners.get(id);
			List<Car> cars = carOwner.getCars();
			cars.forEach(c -> c.setOwner(null));
			owners.remove(id);
			return carOwner.build();
			
		} finally {
			wLock.unlock();
		}
	}

	private void hasCarOwner(long id) {
		if (!owners.containsKey(id)) {
			throw new NotFoundException(String.format("person %d doesn't exists", id));
		}
	}

	@Override
	public CarDto deleteCar(String carNumber) {
		try {
			wLock.lock();
			hasCar(carNumber);
			Car car = cars.get(carNumber);
			CarOwner carOwner = car.getOwner();

			carOwner.getCars().remove(car);
			cars.remove(carNumber);
			return car.build();
		} finally {
			wLock.unlock();
		}
	}

	private void hasCar(String carNumber) {
		if (!cars.containsKey(carNumber)) {
			throw new NotFoundException(String.format("car %s doesn't exists", carNumber));
		}
	}

	@Override
	public TradeDealDto purchase(TradeDealDto tradeDeal) {
		log.debug("purchase: received car {}, owner {}", tradeDeal.carNumber(), tradeDeal.personId());
		Long personId = tradeDeal.personId();
		String carNumber = tradeDeal.carNumber();
		CarOwner carOwner = null;
		try {
			wLock.lock();
			hasCar(carNumber);
			Car car = cars.get(carNumber);
			TradeDeal deal = new TradeDeal(tradeDeal, car.getModel());

			CarOwner oldOwner = car.getOwner();
			checkSameOwner(personId, oldOwner);
			if (oldOwner != null) {
				oldOwner.getCars().remove(car);
			}
			if (personId != null) {

				log.debug("new owner exists");
				hasCarOwner(personId);
				carOwner = owners.get(personId);
				carOwner.getCars().add(car);
			}
			car.setOwner(carOwner);
			trades.computeIfAbsent(car, (k) -> new LinkedList<TradeDeal>()).add(deal);
			log.debug("purchase: tradeDeal added: {}", deal);
			return tradeDeal;
		} finally {
			wLock.unlock();
		}
	}

	private void checkSameOwner(Long personId, CarOwner oldOwner) {
		if ((oldOwner == null && personId == null) || (oldOwner != null && personId == oldOwner.getId())) {
			throw new IllegalStateException("trade deal with same owner");
		}

	}

	@Override
	public List<CarDto> getOwnerCars(long id) {
		try {
			rLock.lock();
			hasCarOwner(id);
			return owners.get(id).getCars().stream().map(Car::build).toList();
		} finally {
			rLock.unlock();
		}

	}

	@Override
	public PersonDto getCarOwner(String carNumber) { 
		try {
			rLock.lock();
			hasCar(carNumber);
			Car car = cars.get(carNumber);
			CarOwner carOwner = car.getOwner();
			return carOwner != null ? carOwner.build() : null;
		} finally {
			rLock.unlock();
		}

	}

	@Override
	public List<String> mostPopularModels() {
		HashMap<String, Integer> groupingMap = new HashMap<String, Integer>();
		try {
			rLock.lock();
			trades.entrySet().stream().forEach(e -> groupingMap.compute(e.getKey().getModel(),
					(k, v) -> v == null ? e.getValue().size() : v + e.getValue().size()));
		} finally {
			rLock.unlock();
		}
		HashMap<Integer, List<String>> modelsMap = new HashMap<Integer, List<String>>();

		groupingMap.entrySet().stream().forEach(e -> {
			modelsMap.computeIfAbsent(e.getValue(), v -> new LinkedList<String>()).add(e.getKey());
		});
		int max = 0;
		for (var entry : modelsMap.entrySet()) {
			if (max <= entry.getKey()) {
				max = entry.getKey();
			}
		}
		var res = modelsMap.get(max);
		log.debug("mostPopularModels: returned: {}", res);
		return res;
	}

}

package telran.cars.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import telran.cars.dto.*;
import telran.cars.exceptions.*;
import telran.cars.repo.*;
import telran.cars.service.model.*;
@Service
@RequiredArgsConstructor
@Slf4j
public class CarsServiceImpl implements CarsService {
	final CarRepo carRepo;
	final CarOwnerRepo carOwnerRepo;
	final ModelRepo modelRepo;
	final TradeDealRepo tradeDealRepo;
	@Override
	@Transactional
	public PersonDto addPerson(PersonDto personDto) {
		if(carOwnerRepo.existsById(personDto.id())) {
			throw new IllegalPersonsStateException();
		}
		CarOwner carOwner = CarOwner.of(personDto);
		carOwnerRepo.save(carOwner);
		log.debug("person {} has been saved", personDto);
		return personDto;
	}

	@Override
	@Transactional
	public CarDto addCar(CarDto carDto) {
		if(carRepo.existsById(carDto.number())) {
			throw new IllegalCarsStateException();
		}
		Model model = modelRepo.findById(new ModelYear(carDto.model(), carDto.year()))
				.orElseThrow(() -> new ModelNotFoundException());
		Car car = Car.of(carDto);
		car.setModel(model);
		carRepo.save(car);
		log.debug("car {} has been saved", carDto);
		return carDto;
	}

	@Override
	@Transactional
	public PersonDto updatePerson(PersonDto personDto) {
		CarOwner carOwner = carOwnerRepo.findById(personDto.id())
				.orElseThrow(() -> new PersonNotFoundException());
		carOwner.setEmail(personDto.email());
		return personDto;
	}

	@Override
	@Transactional
	public PersonDto deletePerson(long id) {
		log.debug("deletePerson: reseived id {}", id);
		CarOwner owner = carOwnerRepo.findById(id).orElseThrow(() -> {
			log.warn("deletePerson: person with id {} not found", id);
			return new PersonNotFoundException();
		});
		List<Car> cars = carRepo.findByCarOwnerId(id);
		cars.forEach(c -> c.setCarOwner(null));
		List<TradeDeal> tradeDeals = tradeDealRepo.findByCarOwnerId(id);
		tradeDeals.forEach(td -> td.setCarOwner(null));
		carOwnerRepo.delete(owner);
		log.debug("deletePerson: {} succesfully removed", id);
		return owner.build();
	}

	@Override
	@Transactional
	public CarDto deleteCar(String carNumber) {
		log.debug("deleteCar: reseived carNumber {}", carNumber);
		Car car = carRepo.findById(carNumber).orElseThrow(() -> {
			log.warn("deleteCar: car with carNumber {} not found", carNumber);
			return new CarNotFoundException();
		});
		List<TradeDeal> tradeDeals = tradeDealRepo.findByCarNumber(carNumber);
		tradeDealRepo.deleteAll(tradeDeals);
		carRepo.delete(car);
		log.debug("deleteCar: {} succesfully removed", carNumber);
		return car.build();
	}

	@Override
	@Transactional
	public TradeDealDto purchase(TradeDealDto tradeDealDto) {
		log.debug("purchase: received tradeDeal with carNumber {}, personId {}", tradeDealDto.carNumber(), tradeDealDto.personId());
		Car car = carRepo.findById(tradeDealDto.carNumber())
				.orElseThrow(() -> {
					log.warn("purchase: car {} not found", tradeDealDto.carNumber());
					return new CarNotFoundException();
				});
		CarOwner carOwner = null;
		Long personId = tradeDealDto.personId();
		if ( personId != null) {
			carOwner = carOwnerRepo.findById(personId)
					.orElseThrow(() -> {
						log.warn("purchase: owner {} not found", tradeDealDto.personId());
						return new PersonNotFoundException();
					});
			if(car.getCarOwner().getId() == personId) {
				log.warn("purchase: trade with the same owner, id {} not found", tradeDealDto.personId());
				throw new TradeDealIllegalStateException();
			}
		}
		car.setCarOwner(carOwner);
		TradeDeal tradeDeal = new TradeDeal();
		tradeDeal.setCar(car);
		tradeDeal.setCarOwner(carOwner);
		tradeDeal.setDate(LocalDate.parse(tradeDealDto.date()));
		tradeDealRepo.save(tradeDeal);
		log.debug("purchase: received tradeDeal with carNumber {}, personId {} succesfully completed", tradeDealDto.carNumber(), tradeDealDto.personId());
		return tradeDealDto;
	}

	@Override
	@Transactional
	public List<CarDto> getOwnerCars(long id) {
		log.debug("getOwnerCars: received id {}", id);
		
		if (!carOwnerRepo.existsById(id)) {
				log.warn("getOwnerCars: person with id {} not found", id);
				throw new PersonNotFoundException();		
		}
		List<Car> cars = carRepo.findByCarOwnerId(id);
		
		if (cars.isEmpty()) {
			log.warn("getOwnerCars: empty list of cars by id {}", id);
		}
		log.debug("getOwnerCars: received {} cars for owner id {}", cars.size(), id);
		return cars.stream().map(c -> c.build()).toList();
	}

	@Override
	@Transactional
	public PersonDto getCarOwner(String carNumber) {
		log.debug("getCarOwner: reseived carNumber {}", carNumber);
		Car car = carRepo.findById(carNumber).orElseThrow(() -> {
			log.warn("getCarOwner: car with carNumber {} not found", carNumber);
			return new CarNotFoundException();
		});
		CarOwner owner = car.getCarOwner();
		
		if (owner == null) {
			log.warn("getCarOwner: owner to car with carNumber {} not exists", carNumber);
			throw new PersonNotFoundException();
		}
		log.debug("getCarOwner: owner id to carNumber {} is {}", carNumber, owner.getId());
		return owner.build();
	}

	@Override
	public List<String> mostPopularModels() {
		// Not Implemented yet
		
		return null;
	}

	@Override
	@Transactional
	public ModelDto addModel(ModelDto modelDto) {
		log.debug("addModel: reseived model {} - {}", modelDto.getModelName(), modelDto.getModelYear());
		Model model = Model.of(modelDto);
		
		if (modelRepo.existsById(model.getModelYear())) {
			log.warn("addModel: model {} - {} already exists", modelDto.getModelName(), modelDto.getModelYear());
			throw new IllegalModelsStateException();
		}
		modelRepo.save(model);
		log.debug("addModel: model {} - {} succesfully saved", modelDto.getModelName(), modelDto.getModelYear());
		return modelDto;
	}

}

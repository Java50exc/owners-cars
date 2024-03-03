package telran.cars.service.model;

import java.util.Objects;

import lombok.Getter;
import telran.cars.dto.CarDto;

@Getter
public class Car {
	String number;
	String model;
	CarOwner owner;
	public Car(CarDto carDto) {
		number = carDto.number();
		model = carDto.model();
	}
	public CarDto build() {
		return new CarDto(number, model);
	}
	public void setOwner(CarOwner owner) {
		this.owner = owner;
	}
	@Override
	public int hashCode() {
		return Objects.hash(number);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Car other = (Car) obj;
		return Objects.equals(number, other.number);
	}
	
	
}

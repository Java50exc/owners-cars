package telran.cars.service.model;

import java.time.LocalDate;

import lombok.Getter;
import telran.cars.dto.TradeDealDto;

@Getter
public class TradeDeal {
	private Long personId;
	private String carNumber;
	private String carModel;
	private LocalDate date;
	
	public TradeDeal(TradeDealDto tradeDealDto, String model) {
		this.personId = tradeDealDto.personId();
		this.carNumber = tradeDealDto.carNumber();
		this.carModel = model;
		this.date = LocalDate.now();
	}
	
	public TradeDealDto build() {
		return new TradeDealDto(carNumber, personId);
	}
	

}

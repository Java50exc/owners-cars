package telran.cars.dto;

import jakarta.validation.constraints.*;
import static telran.cars.api.ValidationConstants.*;

import java.util.Objects;
public record CarDto(
		@NotEmpty(message=MISSING_CAR_NUMBER_MESSAGE) @Pattern(regexp = CAR_NUMBER_REGEXP,message=WRONG_CAR_NUMBER_MESSAGE) String number,
		@NotEmpty (message=MISSING_CAR_MODEL_MESSAGE) String modelName, 
		@NotNull (message=MISSING_CAR_YEAR_MESSAGE) @Min(value=MIN_MODEL_YEAR_VALUE, message=WRONG_MIN_MODEL_YEAR_VALUE_MESSAGE) Integer modelYear, 
		String color,
		@PositiveOrZero(message=NEGATIVE_CAR_KILOMETERS_MESSAGE) Integer kilometers,
		 CarState carState) {

	
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
		CarDto other = (CarDto) obj;
		return Objects.equals(number, other.number);
	}
}



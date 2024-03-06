package telran.cars.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import static telran.cars.api.ValidationConstants.*;

public record ModelDto(@NotBlank(message = MISSING_CAR_MODEL_MESSAGE) String modelName,
		@NotBlank(message = MISSING_CAR_YEAR_MESSAGE) String modelYear,
		@NotBlank(message = MISSING_CAR_COMPANY_MESSAGE) String company,
		@NotNull(message = MISSING_CAR_ENGINE_POWER_MESSAGE) @PositiveOrZero(message = NEGATIVE_CAR_ENGINE_POWER_MESSAGE) Integer enginePower,
		@NotNull(message = MISSING_CAR_ENGINE_CAPACITY_MESSAGE) @PositiveOrZero(message = NEGATIVE_CAR_ENGINE_CAPACITY_MESSAGE) Integer engineCapacity) {

}

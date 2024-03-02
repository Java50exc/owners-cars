package telran.cars.dto;

import jakarta.validation.constraints.*;
import static telran.cars.api.ValidationConstants.*;

public record PersonDto(
		@NotNull(message = MISSING_PERSON_ID_MESSAGE) @Min(value = MIN_PERSON_ID_VALUE, message = WRONG_MIN_PERSON_ID_VALUE_MESSAGE) @Max(value = MAX_PERSON_ID_VALUE, message = WRONG_MAX_PERSON_ID_VALUE_MESSAGE) Long id,
		@NotEmpty(message = MISSING_PERSON_NAME_MESSAGE) String name,
		@NotEmpty(message = MISSING_BIRTH_DATE_MESSAGE) @Pattern(regexp = BIRTH_DATE_REGEXP, message = WRONG_DATE_FORMAT_MESSAGE) String birthDate,
		@NotEmpty(message = MISSING_PERSON_EMAIL_MESSAGE) @Email(message = WRONG_EMAIL_FORMAT_MESSAGE) String email) {

}

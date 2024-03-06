package telran.cars.service.model;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@NoArgsConstructor
@Table(name = "trade_deals")
public class TradeDeal {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	long id;
	@ManyToOne
	@JoinColumn(name="car_number", nullable = false)
	@Setter
	Car car;
	@ManyToOne
	@JoinColumn(name="owner_id")
	@Setter
	CarOwner carOwner;
	@Temporal(TemporalType.DATE)
	@Setter
	LocalDate date;
}

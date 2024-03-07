package telran.cars.repo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import telran.cars.dto.ModelNameAmount;
import telran.cars.service.model.*;

public interface CarRepo extends JpaRepository<Car, String> {
	List<Car> findByCarOwnerId(long id);

	@Query(value = """
			select c.model_name as name, count(*) as amount
			from cars c join car_owners o on c.owner_id = o.id
			where o.birth_date between(:dateFrom, :dateTo)
			group by model_name
			order by amount desc limit :nModels""", nativeQuery = true)
	List<ModelNameAmount> mostPopularModelNameByOwnerAges(int nModels, LocalDate dateFrom, LocalDate dateTo);

	@Query(value = """
			select color from cars
			where model=:model
			group by color
			order by count(*) desc
			limit 1""", nativeQuery = true)
	String oneMostPopularColorModel(String model);


}

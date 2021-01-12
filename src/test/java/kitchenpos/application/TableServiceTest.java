package kitchenpos.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import kitchenpos.MySpringBootTest;
import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

@MySpringBootTest
class TableServiceTest {

	@Autowired
	private TableService tableService;
	@Autowired
	private OrderTableDao orderTableDao;
	@MockBean
	private OrderDao orderDao;

	@Test
	void create() {
		//given
		OrderTable orderTable = new OrderTable();

		//when
		OrderTable savedOrderTable = tableService.create(orderTable);

		//then
		assertThat(tableService.list()).contains(savedOrderTable);
	}

	@DisplayName("주문테이블의 게스트 수를 변경한다.")
	@Test
	void changeNumberOfGuests() {
		OrderTable orderTable = new OrderTable();
		orderTable.setEmpty(false);
		OrderTable savedOrderTable = tableService.create(orderTable);

		int newNumberOfGuests = savedOrderTable.getNumberOfGuests() + 1;
		savedOrderTable.setNumberOfGuests(newNumberOfGuests);

		//when
		OrderTable actual = tableService
			  .changeNumberOfGuests(savedOrderTable.getId(), savedOrderTable);

		//then
		assertThat(actual.getNumberOfGuests()).isEqualTo(newNumberOfGuests);
	}

	@DisplayName("주문테이블의 게스트 수를 0미만으로 변경할 수 없다.")
	@Test
	void changeNumberOfGuestsUnderZero() {
		OrderTable orderTable = new OrderTable();
		OrderTable savedOrderTable = tableService.create(orderTable);
		savedOrderTable.setNumberOfGuests(-1);

		//when, then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> tableService
					.changeNumberOfGuests(savedOrderTable.getId(), savedOrderTable));
	}

	@DisplayName("빈 테이블의 게스트 수를 변경할 수 없다.")
	@Test
	void changeNumberOfGuestsWithEmptyTable() {
		OrderTable orderTable = new OrderTable();
		orderTable.setEmpty(true);
		OrderTable savedOrderTable = tableService.create(orderTable);

		//when, then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> tableService
					.changeNumberOfGuests(savedOrderTable.getId(), savedOrderTable));
	}

	@DisplayName("테이블 상태를 변경한다.")
	@Test
	void changeEmpty() {
		//given
		OrderTable orderTable = tableService.create(new OrderTable());
		orderTable.setEmpty(true);

		when(orderDao.existsByOrderTableIdAndOrderStatusIn(
			  orderTable.getId(),
			  Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
			  .thenReturn(false);

		//when
		OrderTable changedOrderTable = tableService.changeEmpty(orderTable.getId(), orderTable);

		//then
		assertThat(changedOrderTable.isEmpty()).isTrue();
	}

	@DisplayName("단체로 지정된 테이블은 상태를 변경할 수 없다.")
	@Test
	void changeEmptyWithTableGroup() {
		//given
		OrderTable orderTable = orderTableDao.findById(9L).get();
		orderTable.setEmpty(true);

		//when, then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> tableService.changeEmpty(orderTable.getId(), orderTable));
	}

	@DisplayName("조리, 식사 상태의 주문이 있는경우 테이블상태를 변경할 수 없다.")
	@Test
	void changeEmptyWithCookingOrder() {
		//given
		OrderTable orderTable = tableService.create(new OrderTable());
		orderTable.setEmpty(true);

		when(orderDao.existsByOrderTableIdAndOrderStatusIn(
			  orderTable.getId(),
			  Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name())))
			  .thenReturn(true);

		//when, then
		assertThatIllegalArgumentException()
			  .isThrownBy(() -> tableService.changeEmpty(orderTable.getId(), orderTable));
	}
}

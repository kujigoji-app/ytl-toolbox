package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple5<A, B, C, D, E> extends Tuple4<A, B, C, D> {
	private E val5;
}

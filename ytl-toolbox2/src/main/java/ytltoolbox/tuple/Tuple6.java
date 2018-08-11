package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple6<A, B, C, D, E, F> extends Tuple5<A, B, C, D, E> {
	private F val6;
}

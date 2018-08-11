package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple7<A, B, C, D, E, F, G> extends Tuple6<A, B, C, D, E, F> {
	private G val7;
}

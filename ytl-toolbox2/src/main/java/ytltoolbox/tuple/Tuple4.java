package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple4<A, B, C, D> extends Tuple3<A, B, C> {
	private D val4;
}

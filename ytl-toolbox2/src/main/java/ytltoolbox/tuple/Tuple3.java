package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple3<A, B, C> extends Tuple<A, B> {
	private C val3;
}

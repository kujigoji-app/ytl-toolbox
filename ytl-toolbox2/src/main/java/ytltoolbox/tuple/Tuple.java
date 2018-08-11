package ytltoolbox.tuple;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data(staticConstructor = "of")
public class Tuple<A, B> {
	private A val1;
	private B val2;
}

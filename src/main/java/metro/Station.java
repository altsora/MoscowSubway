package metro;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class Station implements Comparable<Station> {
    private String name;
    private Line line;

    @Override
    public int compareTo(Station station) {
        int lineComparison = line.compareTo(station.line);
        return lineComparison != 0 ?
                lineComparison :
                name.compareToIgnoreCase(station.name);
    }
}

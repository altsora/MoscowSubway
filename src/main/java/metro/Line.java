package metro;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(exclude = "stations")
public class Line implements Comparable<Line> {
    private String number;
    private String name;
    private List<Station> stations;

    public Line(String number, String name) {
        this.number = number;
        this.name = name;
        this.stations = new ArrayList<>();
    }

    public void addStation(Station station) {
        stations.add(station);
    }

    private double getDoubleNumberLine() {
        return !number.matches("\\d+") ?
                Double.parseDouble(number.replaceAll("\\D", ".5")) :
                Double.parseDouble(number);
    }

    @Override
    public int compareTo(Line line) {
        return Double.compare(getDoubleNumberLine(), line.getDoubleNumberLine());
    }
}

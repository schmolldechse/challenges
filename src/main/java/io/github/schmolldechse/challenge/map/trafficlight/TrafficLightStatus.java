package io.github.schmolldechse.challenge.map.trafficlight;

public enum TrafficLightStatus {

    RED(
            "Rotlicht",
            4,
            10
    ),
    YELLOW(
            "Gelblicht",
            1,
            4
    ),
    GREEN(
            "Grünlicht",
            150,
            480
    );

    final String name;
    public int minDuration, maxDuration; // in s

    TrafficLightStatus(String name, int minDuration, int maxDuration) {
        this.name = name;
        this.minDuration = minDuration;
        this.maxDuration = maxDuration;
    }

    public void setMinDuration(int minDuration) {
        this.minDuration = minDuration;
    }

    public void setMaxDuration(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    public TrafficLightStatus next() {
        return switch (this) {
            case RED -> GREEN;
            case YELLOW -> RED;
            case GREEN -> YELLOW;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }

    public TrafficLightStatus previous() {
        return switch (this) {
            case RED -> YELLOW;
            case YELLOW -> GREEN;
            case GREEN -> RED;
            default -> throw new IllegalStateException("Unexpected value: " + this);
        };
    }
}

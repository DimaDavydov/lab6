package ru.dima.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class Shorties implements Information {
    @JsonProperty(required = true)
    protected String shortyName;

    @JsonProperty(required = true)
    protected Positions.QueuePosition shortyPosition;

    @JsonProperty(required = true)
    protected int age;

    public Shorties(String ShortyName, Positions.QueuePosition ShortyPosition, int Age) {
        if (ShortyName.equals("")) {
            throw new EmptyStringException("Имя не введено");
        }
        this.shortyName = ShortyName;
        this.shortyPosition = ShortyPosition;
        this.age = Age;
    }

    public String getShortyName() {
        return shortyName;
    }

    public Positions.QueuePosition getShortyPosition() {
        return shortyPosition;
    }

    public int getAge() throws AgeException {
        if (age < 0) {
            throw new AgeException("Недопустимо отрицательное значение возраста");
        }
        return age;
    }

    public void ChangePosition(Positions.QueuePosition position) {
        this.shortyPosition = position;
    }

    @Override
    public void getInformation() {
        System.out.println(getShortyName() + " в " + getShortyPosition() + " вереницы малышей");
    }
}
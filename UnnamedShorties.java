package ru.dima.server.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
public class UnnamedShorties extends Shorties implements ActionsOfUnnamedShorties,
    Comparable<UnnamedShorties> {

  @JsonProperty(required = true)
  private int weight;


  public UnnamedShorties(String name, Positions.QueuePosition position, int age, int weight) {
    super(name, position, age);

    this.weight = weight;
  }

  @Override
  public void abut() {
    System.out.println(getShortyName() + " упираются стальными остриями альпенштоков в лед");
  }

  @Override
  public void peg() {
    System.out.println(getShortyName() + " привязывают Знайку позади всех");
  }

  @Override public void notAllow() {
    System.out.println(getShortyName() + " не разрешают Знайке вылезать вперёд");
    Tone.getTone(1);
  }

  @Override
  public void cutDown() {
    System.out.println(getShortyName() + " вырубают во льду ступеньки");
  }

  @Override
  public void stand() {
    System.out.println(getShortyName() + " стоят на ледяных ступеньках");
    Tone.getTone(2);
  }

  @Override
  public void loosen() {
    System.out.println(getShortyName() + " постепенно отпускают верёвку");
  }

  @Override
  public void watch() {
    System.out.println(getShortyName() + " тщательно следят, чтобы верёвка не выскользнула из рук");
  }

  @Override
  public void getInformation() {
    System.out.println(getShortyName() + " в " + getShortyPosition() + " вереницы малышей");
  }

  @Override
  public int hashCode() {
    int hc;
    hc = 5 + 8 * age;
    hc = 7 + hc * shortyName.length() * 3;
    return hc;
  }

  @Override
  public int compareTo(UnnamedShorties unnamedShorties) {
    return Integer.compare(weight, unnamedShorties.weight);
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UnnamedShorties)) {
      return false;
    }
    UnnamedShorties that = (UnnamedShorties) o;
    return getWeight() == that.getWeight();
  }

  @Override public String toString() {
    return "UnnamedShorties{" +
        "weight=" + weight +
        ", shortyName='" + shortyName + '\'' +
        ", shortyPosition=" + shortyPosition +
        ", age=" + age +
        '}';
  }

  public static class Tone {

    public static void getTone(int a) {
      class tone1 {

        void show() {
          System.out.println("вежливо");
        }
      }
      class tone2 {

        void show() {
          System.out.println("в напряжении");
        }
      }
      if (a == 1) {
        tone1 t1 = new tone1();
        t1.show();
      }
      if (a == 2) {
        tone2 t2 = new tone2();
        t2.show();
      }
    }
  }
}
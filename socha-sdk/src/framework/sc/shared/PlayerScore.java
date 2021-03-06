package sc.shared;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import sc.helpers.CollectionHelper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@XStreamAlias(value = "score")
public final class PlayerScore {
  @XStreamImplicit(itemFieldName = "part")
  private final List<BigDecimal> parts;

  @XStreamAsAttribute
  private ScoreCause cause;

  @XStreamAsAttribute
  private String reason;

  /** might be needed by XStream */
  public PlayerScore() {
    parts = null;
  }

  public PlayerScore(boolean winner, String reason) {
    this(ScoreCause.REGULAR, reason, 1);
  }

  public PlayerScore(ScoreCause cause, String reason, Integer... scores) {
    this(cause, reason, CollectionHelper.iterableToCollection(
            CollectionHelper.intArrayToBigDecimalArray(scores)).toArray(
            new BigDecimal[scores.length]));
  }

  public PlayerScore(ScoreCause cause, String reason, BigDecimal... parts) {
    if (parts == null) {
      throw new IllegalArgumentException("scores must not be null");
    }

    this.parts = Arrays.asList(parts);
    this.cause = cause;
    this.reason = reason;
  }

  public int size() {
    return this.parts.size();
  }

  public ScoreCause getCause() {
    return this.cause;
  }

  public String getReason() {
    return this.reason;
  }

  public String[] toStrings() {
    return CollectionHelper.iterableToCollection(CollectionHelper.map(parts, val -> val.toString())).toArray(new String[parts.size()]);
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    String[] strings = this.toStrings();
    for (int i = 0; i < strings.length; i++) {
      if (i > 0) result.append("; ");
      result.append(strings[i]);
    }
    return result.toString();
  }

  public void setCause(ScoreCause cause) {
    this.cause = cause;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public List<BigDecimal> getValues() {
    return Collections.unmodifiableList(parts);
  }

  public void setValueAt(int index, BigDecimal v) {
    parts.set(index, v);
  }

  public boolean matches(ScoreDefinition definition) {
    return size() == definition.size();
  }

  @Override
  public boolean equals(Object eq) {
    if (eq instanceof PlayerScore) {
      PlayerScore score = (PlayerScore) eq;
      if (!this.getCause().equals(score.getCause()) ||
              !(this.getValues().size() == score.getValues().size())) {
        return false;
      }
      if (!this.getReason().equals(score.getReason())) { // may be null
        return false;
      }
      for (int i = 0; i < this.parts.size(); i++) {
        if (!this.getValues().get(i).equals(score.getValues().get(i))) {
          return false;
        }
      }
      return true;
    } else {
      return false;
    }
  }

}

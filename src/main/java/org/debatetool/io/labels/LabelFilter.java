package org.debatetool.io.labels;

import org.debatetool.core.Card;

import java.util.List;

public interface LabelFilter {
    void addLabel(String label);
    void setLabels(String... labels);
    List<Card> applyFilter (List<Card> list);
}

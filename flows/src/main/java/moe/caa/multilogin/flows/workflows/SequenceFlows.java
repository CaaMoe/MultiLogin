package moe.caa.multilogin.flows.workflows;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

/**
 * 代表顺序流
 */
public class SequenceFlows<C> extends BaseFlows<C> {
    @Getter
    private final List<BaseFlows<C>> steps;

    public SequenceFlows(List<BaseFlows<C>> steps) {
        this.steps = Collections.unmodifiableList(steps);
    }

    @Override
    public Signal run(C context) {
        for (BaseFlows<C> step : steps) {
            Signal signal = step.run(context);
            // PASS， 继续执行
            if (signal == Signal.PASSED) continue;
            // 中断
            if (signal == Signal.TERMINATED) return Signal.TERMINATED;
        }
        return Signal.PASSED;
    }
}

package core.blockcontents;

import io.componentio.ComponentIOManager;

import java.io.IOException;

public class BlockAnalytic implements BlockComponent {
    protected String content;

    public BlockAnalytic(String content) {
        this.content = content;
    }

    @Override
    public String getDisplayContent() {
        return content;
    }

    @Override
    public String getBlockStorageString() {
        return content;
    }

    @Override
    public void loadExternal(ComponentIOManager manager) throws IOException {
        // nothing to do
    }
}

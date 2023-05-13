package com.elishaazaria.sayboard.ime.recognizers.providers;

import com.elishaazaria.sayboard.Tools;
import com.elishaazaria.sayboard.data.LocalModel;
import com.elishaazaria.sayboard.data.LocalModelType;
import com.elishaazaria.sayboard.ime.IME;
import com.elishaazaria.sayboard.ime.recognizers.RecognizerSource;
import com.elishaazaria.sayboard.ime.recognizers.VoskLocal;
import com.elishaazaria.sayboard.ime.recognizers.WhisperLocal;

import java.util.List;

public class VoskLocalProvider implements RecognizerSourceProvider {
    private final IME ime;

    public VoskLocalProvider(IME ime) {
        this.ime = ime;
    }

    @Override
    public void loadSources(List<RecognizerSource> recognizerSources) {
        for (LocalModel localModel : Tools.getInstalledModelsList(ime)) {
            if (localModel.modelType == LocalModelType.VOSK) {
                recognizerSources.add(new VoskLocal(localModel));
            } else {
                recognizerSources.add(new WhisperLocal(localModel));
            }
        }
    }
}

package cn.qihuang02.cyyn.datagen;

import cn.qihuang02.cyyn.CallYouByYourName;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

public class CYYNLangProvider extends LanguageProvider {
    public CYYNLangProvider(PackOutput output) {
        super(output, CallYouByYourName.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("message.cyyn.cooldown", "Please wait %s seconds before mentioning again.");
        add("message.cyyn.notified", "%s mentioned you: %s");
        add("message.cyyn.notified.reply_tooltip", "Click to reply to the mention");
        add("message.cyyn.group.denied", "You do not have permission to use group mentions.");
    }
}

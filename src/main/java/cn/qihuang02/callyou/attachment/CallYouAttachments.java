package cn.qihuang02.callyou.attachment;

import cn.qihuang02.callyou.CallYouByYourName;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class CallYouAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CallYouByYourName.MODID);

    public static final Supplier<AttachmentType<MentionPreferences>> MENTION_PREFERENCES =
            ATTACHMENT_TYPES.register(
                    "mention_preferences",
                    () -> AttachmentType
                            .builder(MentionPreferences::new)
                            .serialize(MentionPreferences.CODEC)
                            .copyOnDeath()
                            .build()
            );
}

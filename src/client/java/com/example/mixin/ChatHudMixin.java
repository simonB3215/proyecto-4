package com.example.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatHud.class)
public class ChatHudMixin {
    @Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    private void onAddMessage(Text message, CallbackInfo ci) {
        if (com.example.client.ExampleModClient.isTranslatingOutput) return;

        // Enviar a nuestro analizador global en tiempo real y cancelar si corresponde
        if (com.example.client.ExampleModClient.shouldCancelAndTranslate(message.getString())) {
            ci.cancel();
        }
    }
}

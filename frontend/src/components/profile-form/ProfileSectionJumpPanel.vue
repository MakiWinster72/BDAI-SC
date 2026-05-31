<script setup>
defineProps({
  open: {
    type: Boolean,
    required: true,
  },
  items: {
    type: Array,
    required: true,
  },
});

const emit = defineEmits(["close", "jump"]);

const PANEL_TRANSITION_MS = { enter: 450, leave: 400 };

function onDismiss() {
  emit("close");
}

function onJump(sectionId) {
  emit("jump", sectionId);
}
</script>

<template>
  <Teleport to="body">
    <Transition
      name="profile-jump-slide"
      appear
      :duration="PANEL_TRANSITION_MS"
    >
      <div v-if="open" class="profile-jump-shell">
        <div
          class="profile-jump-dismiss"
          aria-hidden="true"
          @click="onDismiss"
        />
        <aside
          class="profile-jump-panel"
          role="dialog"
          aria-modal="true"
          aria-label="页面大纲"
        >
          <div class="profile-jump-panel-header">页面大纲</div>
          <ul class="profile-jump-tree">
            <li
              v-for="item in items"
              :key="item.id"
              class="profile-jump-tree-node"
            >
              <button
                type="button"
                class="profile-jump-tree-link"
                @click="onJump(item.id)"
              >
                <span class="profile-jump-tree-branch" aria-hidden="true" />
                <span class="profile-jump-tree-label">{{ item.label }}</span>
              </button>
            </li>
          </ul>
        </aside>
      </div>
    </Transition>
  </Teleport>
</template>

<style src="@/assets/styles/profile-section-jump-panel.css"></style>

import { computed, watch } from "vue";

export function useProfilePartyFields(info, isEditing) {
  const leagueApplicationDisabled = computed(
    () => !isEditing.value || !info.leagueJoined,
  );
  const leagueJoinDisabled = computed(
    () =>
      leagueApplicationDisabled.value ||
      !info.leagueApplicationDate ||
      info.leagueDeveloping,
  );
  const leagueNoDisabled = computed(
    () =>
      !isEditing.value ||
      !info.leagueJoinDate ||
      info.leagueDeveloping ||
      !info.leagueJoined,
  );
  const partyAppliedDisabled = computed(() => !isEditing.value);
  const applicationDateDisabled = computed(
    () => !isEditing.value || partyAppliedDisabled.value || !info.partyApplied,
  );
  const activistDateDisabled = computed(
    () =>
      !isEditing.value ||
      !info.applicationDate ||
      info.activistDeveloping ||
      partyAppliedDisabled.value ||
      !info.partyApplied,
  );
  const partyTrainingDisabled = computed(
    () =>
      !isEditing.value ||
      !info.activistDate ||
      info.activistDeveloping ||
      info.partyTrainingPending ||
      partyAppliedDisabled.value ||
      !info.partyApplied,
  );
  const developmentTargetDisabled = computed(
    () =>
      !isEditing.value ||
      !info.partyTrainingDate ||
      info.partyTrainingPending ||
      info.developmentTargetDeveloping ||
      info.activistDeveloping ||
      partyAppliedDisabled.value ||
      !info.partyApplied,
  );
  const probationaryDisabled = computed(
    () =>
      !isEditing.value ||
      !info.developmentTargetDate ||
      info.developmentTargetDeveloping ||
      info.probationaryDeveloping ||
      info.partyTrainingPending ||
      info.activistDeveloping ||
      partyAppliedDisabled.value ||
      !info.partyApplied,
  );
  const fullMemberDisabled = computed(
    () =>
      !isEditing.value ||
      !info.probationaryMemberDate ||
      info.probationaryDeveloping ||
      info.fullMemberDeveloping ||
      info.developmentTargetDeveloping ||
      info.partyTrainingPending ||
      info.activistDeveloping ||
      partyAppliedDisabled.value ||
      !info.partyApplied,
  );

  watch(
    () => info.leagueDeveloping,
    (next) => {
      if (!next) {
        return;
      }
      info.leagueJoinDate = "";
      info.leagueNo = "";
      info.partyApplied = false;
      info.applicationDate = "";
      info.activistDate = "";
      info.partyTrainingDate = "";
      info.developmentTargetDate = "";
      info.probationaryMemberDate = "";
      info.fullMemberDate = "";
      info.activistDeveloping = false;
      info.partyTrainingPending = false;
      info.developmentTargetDeveloping = false;
      info.probationaryDeveloping = false;
      info.fullMemberDeveloping = false;
    },
  );

  watch(
    () => info.activistDeveloping,
    (next) => {
      if (!next) {
        return;
      }
      info.activistDate = "";
      info.partyTrainingDate = "";
      info.developmentTargetDate = "";
      info.probationaryMemberDate = "";
      info.fullMemberDate = "";
      info.partyTrainingPending = false;
      info.developmentTargetDeveloping = false;
      info.probationaryDeveloping = false;
      info.fullMemberDeveloping = false;
    },
  );

  watch(
    () => info.partyTrainingPending,
    (next) => {
      if (!next) {
        return;
      }
      info.partyTrainingDate = "";
      info.developmentTargetDate = "";
      info.probationaryMemberDate = "";
      info.fullMemberDate = "";
      info.developmentTargetDeveloping = false;
      info.probationaryDeveloping = false;
      info.fullMemberDeveloping = false;
    },
  );

  watch(
    () => info.developmentTargetDeveloping,
    (next) => {
      if (!next) {
        return;
      }
      info.developmentTargetDate = "";
      info.probationaryMemberDate = "";
      info.fullMemberDate = "";
      info.probationaryDeveloping = false;
      info.fullMemberDeveloping = false;
    },
  );

  watch(
    () => info.probationaryDeveloping,
    (next) => {
      if (!next) {
        return;
      }
      info.probationaryMemberDate = "";
      info.fullMemberDate = "";
      info.fullMemberDeveloping = false;
    },
  );

  watch(
    () => info.fullMemberDeveloping,
    (next) => {
      if (!next) {
        return;
      }
      info.fullMemberDate = "";
    },
  );

  function sanitizePartyPayload(payload) {
    if (leagueApplicationDisabled.value) {
      payload.leagueApplicationDate = null;
    }
    if (leagueJoinDisabled.value) {
      payload.leagueJoinDate = null;
    }
    if (leagueNoDisabled.value) {
      payload.leagueNo = null;
    }
    if (partyAppliedDisabled.value) {
      payload.partyApplied = false;
    }
    if (applicationDateDisabled.value) {
      payload.applicationDate = null;
    }
    if (activistDateDisabled.value) {
      payload.activistDate = null;
    }
    if (partyTrainingDisabled.value) {
      payload.partyTrainingDate = null;
    }
    if (developmentTargetDisabled.value) {
      payload.developmentTargetDate = null;
    }
    if (probationaryDisabled.value) {
      payload.probationaryMemberDate = null;
    }
    if (fullMemberDisabled.value) {
      payload.fullMemberDate = null;
    }
    if (!info.leagueJoined) {
      payload.leagueJoined = false;
      payload.leagueApplicationDate = null;
      payload.leagueJoinDate = null;
      payload.leagueDeveloping = false;
      payload.leagueNo = null;
      payload.partyApplied = false;
      payload.applicationDate = null;
      payload.activistDate = null;
      payload.activistDeveloping = false;
      payload.partyTrainingDate = null;
      payload.partyTrainingPending = false;
      payload.developmentTargetDate = null;
      payload.developmentTargetDeveloping = false;
      payload.probationaryMemberDate = null;
      payload.probationaryDeveloping = false;
      payload.fullMemberDate = null;
      payload.fullMemberDeveloping = false;
    } else if (!info.partyApplied) {
      payload.partyApplied = false;
      payload.applicationDate = null;
      payload.activistDate = null;
      payload.activistDeveloping = false;
      payload.partyTrainingDate = null;
      payload.partyTrainingPending = false;
      payload.developmentTargetDate = null;
      payload.developmentTargetDeveloping = false;
      payload.probationaryMemberDate = null;
      payload.probationaryDeveloping = false;
      payload.fullMemberDate = null;
      payload.fullMemberDeveloping = false;
    }
  }

  return {
    leagueApplicationDisabled,
    leagueJoinDisabled,
    leagueNoDisabled,
    partyAppliedDisabled,
    applicationDateDisabled,
    activistDateDisabled,
    partyTrainingDisabled,
    developmentTargetDisabled,
    probationaryDisabled,
    fullMemberDisabled,
    sanitizePartyPayload,
  };
}

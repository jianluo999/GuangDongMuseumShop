export default createStore({
  state: {
    user: null,
    roles: []
  },
  mutations: {
    setUser(state, user) {
      state.user = user;
    },
    setRoles(state, roles) {
      state.roles = roles;
    }
  }
}); 
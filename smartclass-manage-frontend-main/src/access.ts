/**
 * @see https://umijs.org/zh-CN/plugins/plugin-access
 * */
export default function access(initialState: { currentUser?: API.LoginUserVO } | undefined) {
  const { currentUser } = initialState ?? {};
  const role = currentUser?.userRole;
  const isAdmin = role === 'admin';
  const isTeacher = role === 'teacher';

  return {
    canUser: currentUser,
    canAdmin: isAdmin,
    canTeacher: isTeacher,
    canCourseManager: isAdmin || isTeacher,
  };
}

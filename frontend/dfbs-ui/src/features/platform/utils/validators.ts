/**
 * Platform-domain validation rules (contract, org code).
 */

/** Alphanumeric + common symbols; no Chinese. */
export const CONTRACT_REGEX = /^[a-zA-Z0-9\-_\s./]+$/;

/** Uppercase letters + Chinese characters only (for org code/short name). */
export const ORG_CODE_REGEX = /^[A-Z\u4e00-\u9fa5]+$/;

export const ContractRule = {
  pattern: CONTRACT_REGEX,
  message: '合同号不能包含中文字符，仅限英文、数字及常用符号',
};

export const OrgCodeRule = {
  pattern: ORG_CODE_REGEX,
  message: '机构代码/简称仅限大写字母和汉字',
};

/** For INHAND/JINGPIN: uppercase letters only (matches SmartInput onlyLetters + uppercase result). */
export const OrgCodeUppercaseRule = {
  pattern: /^[A-Z]+$/,
  message: '必须为大写字母',
};

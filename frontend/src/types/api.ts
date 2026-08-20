export interface LoginRequest {
  tenantId: string;
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
}

export interface DecodedJwtPayload {
  sub: string;
  tenant_id: string;
  exp: number;
}

export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: Record<string, string>;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface PessoaResponse {
  id: string;
  nome: string;
  cpf: string;
  dataNascimento: string;
  email?: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PessoaCreateRequest {
  nome: string;
  cpf: string;
  dataNascimento: string;
  email?: string | null;
}

export interface PessoaUpdateRequest {
  nome: string;
  cpf: string;
  dataNascimento: string;
  email?: string | null;
}

export type TipoBeneficiario = 'TITULAR' | 'DEPENDENTE';

export type StatusBeneficiario = 'ATIVO' | 'INATIVO' | 'SUSPENSO' | 'CANCELADO';

export interface BeneficiarioResponse {
  id: string;
  pessoa: PessoaResponse;
  tenantId: string;
  matricula: string;
  tipo: TipoBeneficiario;
  status: StatusBeneficiario;
  dataAdesao: string;
  createdAt: string;
  updatedAt: string;
}

export interface BeneficiarioCreateRequest {
  pessoaId: string;
  matricula: string;
  tipo: TipoBeneficiario;
  status: StatusBeneficiario;
  dataAdesao: string;
}

export interface BeneficiarioUpdateRequest {
  matricula: string;
  tipo: TipoBeneficiario;
  status: StatusBeneficiario;
  dataAdesao: string;
}

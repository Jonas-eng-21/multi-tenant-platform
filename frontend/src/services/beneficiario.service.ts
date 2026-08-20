import api from './api'
import type { 
  PageResponse, 
  BeneficiarioResponse,
  BeneficiarioCreateRequest,
  BeneficiarioUpdateRequest,
  StatusBeneficiario,
  TipoBeneficiario
} from '../types/api'

interface ListarParams {
  page?: number
  size?: number
  matricula?: string
  status?: StatusBeneficiario
  tipo?: TipoBeneficiario
  sort?: string
}

class BeneficiarioService {
  async listar(params: ListarParams): Promise<PageResponse<BeneficiarioResponse>> {
    const response = await api.get<PageResponse<BeneficiarioResponse>>('/api/beneficiarios', { params })
    return response.data
  }

  async obterPorId(id: string): Promise<BeneficiarioResponse> {
    const response = await api.get<BeneficiarioResponse>(`/api/beneficiarios/${id}`)
    return response.data
  }

  async criar(data: BeneficiarioCreateRequest): Promise<BeneficiarioResponse> {
    const response = await api.post<BeneficiarioResponse>('/api/beneficiarios', data)
    return response.data
  }

  async atualizar(id: string, data: BeneficiarioUpdateRequest): Promise<BeneficiarioResponse> {
    const response = await api.put<BeneficiarioResponse>(`/api/beneficiarios/${id}`, data)
    return response.data
  }

  async excluir(id: string): Promise<void> {
    await api.delete(`/api/beneficiarios/${id}`)
  }
}

export const beneficiarioService = new BeneficiarioService()

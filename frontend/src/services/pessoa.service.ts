import api from './api'
import type { PageResponse, PessoaResponse } from '../types/api'

class PessoaService {
  async listar(
    params: { page?: number; size?: number; nome?: string; cpf?: string; sort?: string } = {}
  ): Promise<PageResponse<PessoaResponse>> {
    const response = await api.get<PageResponse<PessoaResponse>>('/api/pessoas', {
      params: {
        page: params.page ?? 0,
        size: params.size ?? 10,
        nome: params.nome || undefined,
        cpf: params.cpf || undefined,
        sort: params.sort || undefined
      }
    })
    return response.data
  }

  async obterPorId(id: string): Promise<PessoaResponse> {
    const response = await api.get<PessoaResponse>(`/api/pessoas/${id}`)
    return response.data
  }

  async criar(data: import('../types/api').PessoaCreateRequest): Promise<PessoaResponse> {
    const response = await api.post<PessoaResponse>('/api/pessoas', data)
    return response.data
  }

  async atualizar(id: string, data: import('../types/api').PessoaUpdateRequest): Promise<PessoaResponse> {
    const response = await api.put<PessoaResponse>(`/api/pessoas/${id}`, data)
    return response.data
  }

  async excluir(id: string): Promise<void> {
    await api.delete(`/api/pessoas/${id}`)
  }
}

export const pessoaService = new PessoaService()

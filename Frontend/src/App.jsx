import { useEffect, useMemo, useState } from 'react'
import './App.css'

const emptyCategoryForm = { id: '', name: '', description: '' }
const emptyProductForm = {
  id: '',
  categoryId: '',
  name: '',
  price: '',
  stock: 0,
  status: true,
  sku: '',
}

function App() {
  const [categories, setCategories] = useState([])
  const [products, setProducts] = useState([])
  const [categoryForm, setCategoryForm] = useState(emptyCategoryForm)
  const [productForm, setProductForm] = useState(emptyProductForm)
  const [productFilters, setProductFilters] = useState({
    keyword: '',
    categoryId: '',
    status: '',
  })
  const [activeTab, setActiveTab] = useState('categories')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')

  const categoryLookup = useMemo(() => {
    return new Map(categories.map((category) => [String(category.id), category.name]))
  }, [categories])

  useEffect(() => {
    void loadCategories()
    void loadProducts()
  }, [])

  async function requestJson(path, options = {}) {
    const response = await fetch(path, {
      headers: {
        'Content-Type': 'application/json',
        ...(options.headers ?? {}),
      },
      ...options,
    })

    const contentType = response.headers.get('content-type') || ''
    const payload = contentType.includes('application/json')
      ? await response.json().catch(() => null)
      : await response.text().catch(() => '')

    if (!response.ok) {
      const errorMessage =
        (payload && typeof payload === 'object' && payload.message) ||
        (typeof payload === 'string' && payload) ||
        `Request failed with status ${response.status}`
      throw new Error(errorMessage)
    }

    return payload
  }

  async function loadCategories() {
    setLoading(true)
    setMessage('')
    try {
      const data = await requestJson('/api/categories')
      setCategories(Array.isArray(data) ? data : [])
    } catch (error) {
      setMessage(`Categories: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  async function loadProducts(filters = productFilters) {
    setLoading(true)
    setMessage('')
    try {
      const params = new URLSearchParams()
      if (filters.keyword.trim()) {
        params.set('keyword', filters.keyword.trim())
      }
      if (filters.categoryId) {
        params.set('categoryId', filters.categoryId)
      }
      if (filters.status !== '') {
        params.set('status', filters.status)
      }
      const query = params.toString()
      const data = await requestJson(`/api/products${query ? `?${query}` : ''}`)
      setProducts(Array.isArray(data) ? data : [])
    } catch (error) {
      setMessage(`Products: ${error.message}`)
    } finally {
      setLoading(false)
    }
  }

  function resetCategoryForm() {
    setCategoryForm(emptyCategoryForm)
  }

  function resetProductForm() {
    setProductForm(emptyProductForm)
  }

  function editCategory(category) {
    setActiveTab('categories')
    setCategoryForm({
      id: category.id,
      name: category.name ?? '',
      description: category.description ?? '',
    })
  }

  function editProduct(product) {
    setActiveTab('products')
    setProductForm({
      id: product.id,
      categoryId: product.categoryId ?? '',
      name: product.name ?? '',
      price: product.price ?? '',
      stock: product.stock ?? 0,
      status: Boolean(product.status),
      sku: product.sku ?? '',
    })
  }

  async function saveCategory(event) {
    event.preventDefault()
    setMessage('')
    const method = categoryForm.id ? 'PUT' : 'POST'
    const url = categoryForm.id ? `/api/categories/${categoryForm.id}` : '/api/categories'

    try {
      await requestJson(url, {
        method,
        body: JSON.stringify({
          name: categoryForm.name,
          description: categoryForm.description,
        }),
      })
      resetCategoryForm()
      await loadCategories()
      await loadProducts()
      setMessage(categoryForm.id ? 'Category updated.' : 'Category created.')
    } catch (error) {
      setMessage(`Category save failed: ${error.message}`)
    }
  }

  async function saveProduct(event) {
    event.preventDefault()
    setMessage('')
    const method = productForm.id ? 'PUT' : 'POST'
    const url = productForm.id ? `/api/products/${productForm.id}` : '/api/products'

    try {
      await requestJson(url, {
        method,
        body: JSON.stringify({
          categoryId: productForm.categoryId === '' ? null : Number(productForm.categoryId),
          name: productForm.name,
          price: productForm.price === '' ? null : Number(productForm.price),
          stock: Number(productForm.stock),
          status: productForm.status,
          sku: productForm.sku,
        }),
      })
      resetProductForm()
      await loadProducts()
      await loadCategories()
      setMessage(productForm.id ? 'Product updated.' : 'Product created.')
    } catch (error) {
      setMessage(`Product save failed: ${error.message}`)
    }
  }

  async function removeCategory(id) {
    setMessage('')
    try {
      await requestJson(`/api/categories/${id}`, { method: 'DELETE' })
      if (String(categoryForm.id) === String(id)) {
        resetCategoryForm()
      }
      await loadCategories()
      await loadProducts()
      setMessage('Category deleted.')
    } catch (error) {
      setMessage(`Category delete failed: ${error.message}`)
    }
  }

  async function removeProduct(id) {
    setMessage('')
    try {
      await requestJson(`/api/products/${id}`, { method: 'DELETE' })
      if (String(productForm.id) === String(id)) {
        resetProductForm()
      }
      await loadProducts()
      setMessage('Product deactivated.')
    } catch (error) {
      setMessage(`Product delete failed: ${error.message}`)
    }
  }

  function applyProductFilters(event) {
    event.preventDefault()
    void loadProducts(productFilters)
  }

  function clearProductFilters() {
    const nextFilters = { keyword: '', categoryId: '', status: '' }
    setProductFilters(nextFilters)
    void loadProducts(nextFilters)
  }

  return (
    <main className="dashboard">
      <header className="dashboard__header">
        <div>
          <p className="eyebrow">TreeShopManagingSystem</p>
          <h1>Category and Product CRUD Console</h1>
          <p className="subtitle">
            Use this page to create, edit, delete, and inspect categories and products.
          </p>
        </div>
        <div className="dashboard__status">
          <span className={`status-pill ${loading ? 'status-pill--loading' : ''}`}>
            {loading ? 'Loading' : 'Ready'}
          </span>
          <button type="button" className="secondary-button" onClick={() => void loadCategories()}>
            Refresh categories
          </button>
          <button type="button" className="secondary-button" onClick={() => void loadProducts()}>
            Refresh products
          </button>
        </div>
      </header>

      {message ? <div className="notice">{message}</div> : null}

      <nav className="tabs">
        <button
          type="button"
          className={activeTab === 'categories' ? 'tab tab--active' : 'tab'}
          onClick={() => setActiveTab('categories')}
        >
          Categories
        </button>
        <button
          type="button"
          className={activeTab === 'products' ? 'tab tab--active' : 'tab'}
          onClick={() => setActiveTab('products')}
        >
          Products
        </button>
      </nav>

      <section className={activeTab === 'categories' ? 'panel' : 'panel panel--hidden'}>
        <div className="panel__grid">
          <form className="card" onSubmit={saveCategory}>
            <div className="card__header">
              <h2>{categoryForm.id ? 'Edit category' : 'Create category'}</h2>
              {categoryForm.id ? <span className="muted">ID {categoryForm.id}</span> : null}
            </div>
            <label>
              Name
              <input
                value={categoryForm.name}
                onChange={(event) => setCategoryForm({ ...categoryForm, name: event.target.value })}
                placeholder="Indoor plants"
                required
              />
            </label>
            <label>
              Description
              <textarea
                value={categoryForm.description}
                onChange={(event) =>
                  setCategoryForm({ ...categoryForm, description: event.target.value })
                }
                placeholder="Category description"
                rows="4"
              />
            </label>
            <div className="button-row">
              <button type="submit">{categoryForm.id ? 'Update category' : 'Create category'}</button>
              <button type="button" className="secondary-button" onClick={resetCategoryForm}>
                Clear
              </button>
            </div>
          </form>

          <div className="card">
            <div className="card__header">
              <h2>Category table</h2>
              <span className="muted">{categories.length} rows</span>
            </div>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {categories.length === 0 ? (
                    <tr>
                      <td colSpan="4" className="empty">
                        No categories loaded
                      </td>
                    </tr>
                  ) : (
                    categories.map((category) => (
                      <tr key={category.id}>
                        <td>{category.id}</td>
                        <td>{category.name}</td>
                        <td>{category.description || <span className="muted">—</span>}</td>
                        <td>
                          <div className="button-row">
                            <button type="button" className="secondary-button" onClick={() => editCategory(category)}>
                              Edit
                            </button>
                            <button type="button" className="danger-button" onClick={() => removeCategory(category.id)}>
                              Delete
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>

      <section className={activeTab === 'products' ? 'panel' : 'panel panel--hidden'}>
        <div className="panel__grid">
          <form className="card" onSubmit={saveProduct}>
            <div className="card__header">
              <h2>{productForm.id ? 'Edit product' : 'Create product'}</h2>
              {productForm.id ? <span className="muted">ID {productForm.id}</span> : null}
            </div>
            <label>
              Category
              <select
                value={productForm.categoryId}
                onChange={(event) =>
                  setProductForm({ ...productForm, categoryId: event.target.value })
                }
              >
                <option value="">Select a category</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Name
              <input
                value={productForm.name}
                onChange={(event) => setProductForm({ ...productForm, name: event.target.value })}
                placeholder="Monstera"
                required
              />
            </label>
            <div className="form-row">
              <label>
                Price
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={productForm.price}
                  onChange={(event) => setProductForm({ ...productForm, price: event.target.value })}
                  placeholder="150000"
                  required
                />
              </label>
              <label>
                Stock
                <input
                  type="number"
                  min="0"
                  step="1"
                  value={productForm.stock}
                  onChange={(event) => setProductForm({ ...productForm, stock: event.target.value })}
                />
              </label>
            </div>
            <label>
              SKU
              <input
                value={productForm.sku}
                onChange={(event) => setProductForm({ ...productForm, sku: event.target.value })}
                placeholder="SKU-001"
                required
              />
            </label>
            <label className="checkbox-row">
              <input
                type="checkbox"
                checked={productForm.status}
                onChange={(event) => setProductForm({ ...productForm, status: event.target.checked })}
              />
              Active
            </label>
            <div className="button-row">
              <button type="submit">{productForm.id ? 'Update product' : 'Create product'}</button>
              <button type="button" className="secondary-button" onClick={resetProductForm}>
                Clear
              </button>
            </div>
          </form>

          <div className="card">
            <div className="card__header">
              <h2>Product filters</h2>
              <span className="muted">{products.length} rows</span>
            </div>
            <form className="filters" onSubmit={applyProductFilters}>
              <input
                value={productFilters.keyword}
                onChange={(event) =>
                  setProductFilters({ ...productFilters, keyword: event.target.value })
                }
                placeholder="Search keyword"
              />
              <select
                value={productFilters.categoryId}
                onChange={(event) =>
                  setProductFilters({ ...productFilters, categoryId: event.target.value })
                }
              >
                <option value="">All categories</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
              <select
                value={productFilters.status}
                onChange={(event) =>
                  setProductFilters({ ...productFilters, status: event.target.value })
                }
              >
                <option value="">All statuses</option>
                <option value="true">Active only</option>
                <option value="false">Inactive only</option>
              </select>
              <div className="button-row">
                <button type="submit">Apply</button>
                <button type="button" className="secondary-button" onClick={clearProductFilters}>
                  Clear
                </button>
              </div>
            </form>

            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Category</th>
                    <th>Name</th>
                    <th>SKU</th>
                    <th>Price</th>
                    <th>Stock</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {products.length === 0 ? (
                    <tr>
                      <td colSpan="8" className="empty">
                        No products loaded
                      </td>
                    </tr>
                  ) : (
                    products.map((product) => (
                      <tr key={product.id}>
                        <td>{product.id}</td>
                        <td>{categoryLookup.get(String(product.categoryId)) || product.categoryId}</td>
                        <td>{product.name}</td>
                        <td>{product.sku}</td>
                        <td>{Number(product.price).toFixed(2)}</td>
                        <td>{product.stock}</td>
                        <td>{product.status ? 'Active' : 'Inactive'}</td>
                        <td>
                          <div className="button-row">
                            <button type="button" className="secondary-button" onClick={() => editProduct(product)}>
                              Edit
                            </button>
                            <button type="button" className="danger-button" onClick={() => removeProduct(product.id)}>
                              Deactivate
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </section>
    </main>
  )
}

export default App
